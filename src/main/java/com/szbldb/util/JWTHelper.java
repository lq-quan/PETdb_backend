package com.szbldb.util;


import com.szbldb.pojo.userPojo.UserPojo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;



import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTHelper {

    private static final String jwtKey = "szbldb";

    public static String jwtPacker(Map<String, Object> map, int minutes){
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + (long) minutes * 60 * 1000))
                .compact();
    }

    public static UserPojo generateUserPojo(String username){
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        String jwtUser = jwtPacker(map, 60);
        UserPojo userPojo = new UserPojo();
        userPojo.setJwtUser(jwtUser);
        return userPojo;
    }

    public static Claims jwtUnpack(String jwt) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(jwtKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
}
