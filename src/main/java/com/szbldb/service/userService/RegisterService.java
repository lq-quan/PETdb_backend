package com.szbldb.service.userService;

import com.szbldb.dao.UserMapper;
import com.szbldb.exception.UserException;
import com.szbldb.pojo.userPojo.User;
import com.szbldb.pojo.userInfoPojo.UserInfo;
import com.szbldb.service.logService.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;


@Slf4j
@Service
public class RegisterService {

    private final UserMapper userMapper;
    private final LogService logService;
    @Value("${rsa.private-key}")
    private String privateKey;

    @Autowired
    public RegisterService(UserMapper userMapper, LogService logService) {
        this.userMapper = userMapper;
        this.logService = logService;
    }

    /**
     * @Description 检查用户名是否重复
     * @param username 用户名
     * @return boolean
     * @author Quan Li 2024/7/5 16:25
     **/
    public boolean checkIfExisted(String username){
        if(username.length() > 50 || "default".equals(username)) return false;
        return userMapper.getIdByName(username) == null;
    }

    /**
     *
     * @Description 检查密码明文强度
     * @param password 密码明文
     * @return boolean
     * @author Quan Li 2024/7/18 10:38
     **/
    public boolean checkPswIfWeak(String password){
        boolean numFlag = false, charFlag = false;
        if(password.length() < 10) return true;
        for(char ch : password.toCharArray()){
            if((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) charFlag = true;
            else if(ch >= '0' && ch <= '9') numFlag = true;
            else if(ch != '_' && ch != ' ') return true;
        }
        return !numFlag || !charFlag;
    }

    /**
     *
     * @Description 解密 RSA 密码
     * @param encoded 密码 RSA 密文
     * @return java.lang.String
     * @author Quan Li 2024/7/18 11:04
     **/
    public String decodeRSAPsw(String encoded){
        byte[] encodedBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(spec);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encoded));
            String originPsw = new String(decryptedBytes);
            System.out.println(originPsw);
            return originPsw;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @Description 用户注册并初始化用户信息
     * @param username 用户名
     * @param password 经 Bcrypt 加密的密码
     * @param email 邮箱
     * @return boolean
     * @author Quan Li 2024/7/5 16:25
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean signup(String username, String password, String email){
        User user = new User(username, password, email);
        userMapper.insertUser(user);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setName(username);
        userMapper.initUserInfo(userInfo);
        return true;
    }

    /**
     *
     * @Description 查询管理员列表
     * @param page （第）页数
     * @param limit 每页项数
     * @return java.util.List<com.szbldb.pojo.userPojo.User>
     * @author Quan Li 2024/7/17 10:33
     **/
    public List<User> getAdmins(Integer page, Integer limit){
        return userMapper.getAdmins(limit, (page - 1) * limit);
    }


    /**
     *
     * @Description 创建管理员账号
     * @param user 账号信息
     * @return boolean
     * @author Quan Li 2024/7/17 10:33
     **/
    @Transactional(rollbackFor = Exception.class)
    public boolean createAdmin(User user){
        String username = user.getUsername();
        if(!checkIfExisted(username)) return false;
        String originPsw = decodeRSAPsw(user.getPassword());
        System.out.println(originPsw);
        if(checkPswIfWeak(originPsw)) return false;
        String encodedPsw = BCrypt.hashpw(DigestUtils.sha256Hex(originPsw + "petdatabase"), BCrypt.gensalt());
        signup(username, encodedPsw, user.getEmail());
        userMapper.changeRolesByUsername(username);
        userMapper.insertAdmin(username);
        logService.addLog("创建管理员账号：" + username);
        return true;
    }

    /**
     *
     * @Description 删除指定管理员账号
     * @param id 待删除的账号 id
     * @author Quan Li 2024/7/17 10:34
     **/
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer id){
        if("admin".equals(userMapper.getUsernameById(id))) {
            log.warn("试图删除 root 用户");
            throw new UserException("Cannot delete root");
        }
        String username = userMapper.getUsernameById(id);
        userMapper.clearColl(id);
        userMapper.deleteColl(id);
        userMapper.deleteAdminById(id);
        userMapper.deleteUserinfo(id);
        userMapper.deleteUser(id);
        logService.addLog("回收管理员账号：" + username);
    }
}