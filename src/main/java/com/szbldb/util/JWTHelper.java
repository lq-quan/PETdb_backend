package com.szbldb.util;


import com.szbldb.pojo.userPojo.UserPojo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;



import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTHelper {

    private static final String jwtKey = DigestUtils.sha256Hex("szbldb");

    /**
     *
     * @Description 将负载包装为令牌
     * @param map 令牌需要的负载
     * @param minutes 令牌持续时间
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:33
     **/
    public static String jwtPacker(Map<String, Object> map, int minutes){
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + (long) minutes * 60 * 1000))
                .compact();
    }

    /**
     *
     * @Description 将用户名包装为用户令牌，持续 24 小时
     * @param username 用户名
     * @return com.szbldb.pojo.userPojo.UserPojo
     * @author Quan Li 2024/7/5 16:39
     **/
    public static UserPojo generateUserPojo(String username){
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        String jwtUser = jwtPacker(map, 1440);
        UserPojo userPojo = new UserPojo();
        userPojo.setJwtUser(jwtUser);
        return userPojo;
    }

    /**
     *
     * @Description 解析用户令牌，获取其负载
     * @param jwt 令牌
     * @return io.jsonwebtoken.Claims
     * @author Quan Li 2024/7/5 16:40
     **/
    public static Claims jwtUnpack(String jwt) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(jwtKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

    /**
     *
     * @Description 直接通过解析令牌获取用户名
     * @param token 令牌
     * @return java.lang.String
     * @author Quan Li 2024/7/5 16:41
     **/
    public static String getUsername(String token){
        Claims claims = jwtUnpack(token);
        return claims.get("username", String.class);
    }
}
