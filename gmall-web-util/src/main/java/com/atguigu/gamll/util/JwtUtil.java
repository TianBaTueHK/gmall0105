package com.atguigu.gamll.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Map;

/**
 * 加密的工具类
 */
public class JwtUtil {

    //加密
    public static String encode(String key, Map<String,Object> param,String salt){
        if (salt != null){
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();

        return token;
    }

    //解密
    public static Map<String, Object> dencode(String token, String s, String ip) {
        return null;
    }
}























