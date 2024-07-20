package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.exception.UserException;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.util.JWTHelper;
import com.szbldb.util.MailHelper;
import com.szbldb.util.PSWHelper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserInfoService {

    private final UserMapper userMapper;

    @Value("${minio.server.address}")
    private String ipAddress;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket}")
    private String bucket;


    public UserInfoService(@Autowired UserMapper userMapper){
        this.userMapper = userMapper;
    }

    /**
     *
     * @Description 获取用户信息
     * @param jwtUser 用户令牌
     * @return com.szbldb.pojo.userInfoPojo.UserInfo
     * @author Quan Li 2024/7/5 16:26
     **/
    public UserInfo getUserInfo(String jwtUser){
        String username = JWTHelper.getUsername(jwtUser);
        Integer id = userMapper.getIdByName(username);
        UserInfo info = userMapper.getInfoById(id);
        String avatar = info.getAvatar();
        if(avatar == null) avatar = "default.jpg";
        try(MinioClient client = MinioClient.builder()
                .endpoint("https://" + ipAddress)
                .credentials(accessKey, secretKey)
                .build()){
            String avatarUrl =  client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object("images/" + avatar)
                    .expiry(24, TimeUnit.HOURS)
                    .build());
            info.setAvatar(avatarUrl);
            return info;
        }catch (Exception e){
            log.error("获取头像图片出错", e);
        }
        return info;
    }

    /**
     *
     * @Description 修改用户信息
     * @param token 用户令牌
     * @param userInfo 修改后的信息
     * @author Quan Li 2024/7/5 16:27
     **/
    public void changeInfo(String token, UserInfo userInfo){
        String username = JWTHelper.getUsername(token);
        Integer id = userMapper.getIdByName(username);
        userInfo.setId(id);
        userMapper.changeUserInfo(userInfo);
    }

    /**
     *
     * @Description 获取用户邮箱
     * @param token 用户令牌
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:27
     **/
    public String getEmail(String token){
        String username = JWTHelper.getUsername(token);
        return userMapper.getEmail(username);
    }

    /**
     * @Description 检查用户身份，以修改密码；邮件发送失败，返回 null
     * @param username 用户名
     * @return java.lang.String
     * @author Quan Li 2024/7/10 16:19
     **/
    public String checkBeforeModifyPsw(String username){
        String email = userMapper.getEmail(username);
        String sentCode = MailHelper.sendEmail(email, username);
        if(sentCode == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("code", sentCode);
        map.put("username", username);
        return JWTHelper.jwtPacker(map, 10);
    }

    /**
     *
     *
     * @param jwtCode 含有验证码密文的令牌
     * @param username 用户名
     * @param password 经 SHA256 加密的新密码
     * @param code 用户输入的验证码
     * @return boolean
     * @author Quan Li 2024/7/10 16:24
     **/
    public boolean modifyPsw(String jwtCode, String username, String password, String code){
        if(jwtCode == null || jwtCode.isEmpty()) return false;
        String usernameInJwt = JWTHelper.getUsername(jwtCode);
        if(!username.equals(usernameInJwt)) return false;
        String originPsw = PSWHelper.decodeRSAPsw(password);
        if(PSWHelper.checkPswIfWeak(originPsw)) return false;
        password = BCrypt.hashpw(DigestUtils.sha256Hex(originPsw + "petdatabase"), BCrypt.gensalt());
        if(MailHelper.verifyCode(jwtCode, code)){
            userMapper.modifyPassword(username, password);
            return true;
        }
        return false;
    }


    /**
     *
     * @Description 用户上传头像
     * @param token 用户令牌
     * @param avatar 头像文件用户名
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:27
     **/
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(String token, String avatar) throws Exception {
        String username = JWTHelper.getUsername(token);
        Integer id = userMapper.getIdByName(username);
        if(!avatar.endsWith(".jpg") && !avatar.endsWith(".png") && !avatar.endsWith(".jpeg"))
            throw new UserException("Invalid file!");
        try(MinioClient client = MinioClient.builder()
                .endpoint("https://" + ipAddress)
                .credentials(accessKey, secretKey)
                .build()){
            String newAvatar = username + avatar.substring(avatar.lastIndexOf("."));
            String url =  client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object("images/" + newAvatar)
                    .expiry(1, TimeUnit.HOURS)
                    .build());
            userMapper.changeAvatarById(id, newAvatar);
            return url;
        }catch (Exception e){
            log.error("头像图片上传出错", e);
            throw e;
        }
    }
}
