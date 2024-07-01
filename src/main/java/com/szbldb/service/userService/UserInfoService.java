package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.util.JWTHelper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserInfoService {

    private final UserMapper userMapper;
    private final String ipAddress = InetAddress.getLocalHost().getHostAddress();


    public UserInfoService(@Autowired UserMapper userMapper) throws UnknownHostException {
        this.userMapper = userMapper;
    }

    public UserInfo getUserInfo(String jwtUser){
        String username = JWTHelper.getUsername(jwtUser);
        Integer id = userMapper.getIdByName(username);
        UserInfo info = userMapper.getInfoById(id);
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            String avatarUrl =  client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket("test")
                    .object("images/" + info.getAvatar())
                    .expiry(24, TimeUnit.HOURS)
                    .build());
            info.setAvatar(avatarUrl);
            return info;
        }catch (Exception e){
            log.error("获取头像图片出错", e);
        }
        return info;
    }

    public void changeInfo(String token, UserInfo userInfo){
        String username = JWTHelper.getUsername(token);
        Integer id = userMapper.getIdByName(username);
        userInfo.setId(id);
        userMapper.changeUserInfo(userInfo);
    }

    public String getEmail(String token){
        String username = JWTHelper.getUsername(token);
        return userMapper.getEmail(username);
    }


    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(String token, String avatar) throws Exception {
        String username = JWTHelper.getUsername(token);
        Integer id = userMapper.getIdByName(username);
        try(MinioClient client = MinioClient.builder()
                .endpoint("http://" + ipAddress + ":9000")
                .credentials("lqquan", "12345678")
                .build()){
            String newAvatar = username + avatar.substring(avatar.lastIndexOf("."));
            String url =  client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket("test")
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
