package com.atguigu.gamll.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密测试
 */
public class TestJwt {

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("memberId","1");
        map.put("nickName","hk");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMdd HHmm").format(new Date());

        String encode = JwtUtil.encode("2019gmall0105", map, ip + time);

        System.out.println(encode);
    }
}















