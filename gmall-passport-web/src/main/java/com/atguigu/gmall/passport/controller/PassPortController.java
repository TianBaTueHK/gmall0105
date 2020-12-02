package com.atguigu.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;

import com.atguigu.gamll.util.JwtUtil;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.payment.service.UserServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证中心写为web的controller层是因为现在的验证登录都是社交型的(网站和网站相互之间的)，所以一定要写为controller
 *
 * 如果写为service，只能改项目的内部才能访问，外部的服务器访问不了
 */
@Controller
public class PassPortController {

    @Reference
    UserServer userServer;


    /**
     * 第三方社交登录
     * @param code
     * @param request
     * @return
     */
    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request){

        //授权码换取access_token
        String s3 = "";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","");
        paramMap.put("client_secret","");
        paramMap.put("code",code);
        String access_token_json = HttpclientUtil.doPost(s3,paramMap);

        Map<String,Object> access_map = JSON.parseObject(access_token_json,Map.class);

        //access token换取用户信息
        String uid = (String)access_map.get("uid");
        String access_token = (String)access_map.get("access_token");
        String show_user_url = "";
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String,Object> user_map = JSON.parseObject(user_json,Map.class);

        //将用户信息保存数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setAccessToken("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("id"));
//        umsMember.setGender((String) user_map.get("gender"));
        umsMember.setCreatTime(new Date());

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userServer.checkOauthUser(umsCheck); //检查用户之前是否登录

        if (umsMemberCheck == null){
            //保存之前先要检查用户是否存在,存在则不需要保存了
            /**
             * 注意mybatis的主键返回策略不能跨rpc使用，所以要在控制层得到生成的主键，需要将保存db的对象返回给控制层
             */
            umsMember = userServer.addOauthUser(umsMember);
        }else {
            umsMember = umsMemberCheck;
        }


        //生成jwt的token，并且重定向到首页，携带该token
        String token = "";
        String memberId = umsMember.getId(); //rpc的主键返回策略失效
        String memberName = umsMember.getName();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId); //
        userMap.put("umsMemberName",memberName);

        String ip = request.getHeader("x-forwarded-for");//通过Nginx转发的客服端ip
        if (StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();  //request.getRemoteAddr(ip); //从request中获取ip
            ip = "127.0.0.1";
        }
        //按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2019gmall",userMap,ip);

        //将token存入redis一份
        userServer.addToken(token,memberId);

        return "redirect:https://search//gmall.com:8085/index?token"+token;
    }



    /**
     * 用户登录时，被拦截器拦截，去验证中心进行验证
     * currentIp:原始用户请求的ip地址 ；如果是request.getRemoteAddr(); //从request中获取ip，则不算用户原始请求的地址了
     * @param token
     * @param currentIp
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp){

        //通过jwt校验token真假
        Map<String,String> map = new HashMap<>();

        Map<String,Object> dencode = JwtUtil.dencode(token,"2019gmall110105",currentIp);

        if (dencode != null){
            map.put("status","success");
            map.put("memberId", (String) dencode.get("memberId"));
            map.put("nickName", (String) dencode.get("nickName"));
        }else {
            map.put("status","fail");
        }

        //返回token
        return JSON.toJSONString(map);
    }


    /**
     * a：首页访问登录页，携带ReturnUrl回跳地址
     * @param umsMember
     * @return
     */
    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token = "";

        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userServer.login(umsMember);

        if (umsMemberLogin != null){
            //登录成功

            //用户jwt制作token
            String memberId = umsMemberLogin.getId();
            String umsMemberName = umsMemberLogin.getName();
            Map<String,Object> userMap = new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("umsMemberName",umsMemberName);

            String ip = request.getHeader("x-forwarded-for");//通过Nginx转发的客服端ip
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();  //request.getRemoteAddr(ip); //从request中获取ip
                ip = "127.0.0.1";
            }

            //加密的工具类:加密token
            //按照设计的算法对参数进行加密后，生成token
            token = JwtUtil.encode("2019gmall",userMap,ip);

            //将token存入redis一份
            userServer.addToken(token,memberId);


        }else {
            //登录失败
            token = "fail";
        }

        //返回token
        return token;
    }

    /**
     * b：登录页保存回跳地址
     * @param ReturnUrl
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){

        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }

}































