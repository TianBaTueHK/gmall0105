package com.atguigu.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 授权码的测试
 */
public class TestOauth2 {


    public String getCode(){

        //获取授权码
        String s1 =HttpclientUtil.doGet("") ;

        //返回授权码到回调地址
        String s2 = "";

        return null;
    }

    public String getAccess_token(){
        //c：第三方软件拿着code去（qq）交换access_token
        String s3 = "";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("","");
        String access_token_json = HttpclientUtil.doPost(s3,paramMap);

        Map<String,String> access_map = JSON.parseObject(access_token_json,Map.class);

        return access_map.get("access_token");
    }

    public Map<String, String> getUser_info(){

        //第三方软件拿着access_token去取得用户相关信息
        String s4 = "";
        String user_json = HttpclientUtil.doGet(s4);
        Map<String,String> user_map = JSON.parseObject(user_json,Map.class);

        return user_map;
    }

    public static void main(String[] args) {


    }

}








