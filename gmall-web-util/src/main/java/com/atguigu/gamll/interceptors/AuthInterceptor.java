package com.atguigu.gamll.interceptors;

import com.atguigu.gamll.annotations.LoginRequired;
import com.atguigu.gamll.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义的拦截器类
 * 用户登录被拦截器拦截时的验证
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        //判断被拦截的请求的访问的方法的注解（是否是需要拦截的）
        HandlerMethod hm = (HandlerMethod)handler;
        //通过反射(可以获得类，方法的整体信息)的方法，可以得到这个方法的注解
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        /**
         * 没有那个注解，不需要验证
         */
        if (methodAnnotation == null){
            //直接放行，可以访问
            return true;
        }

        /**
         * 判断是否登录过
         * 因为必须登录成功才可以使用
         */
        //先设token值为空，没登录过
        String token = "";

        String oldToken = String.valueOf(CookieUtil.getCookieByName(request,"oldToken",true));
        if (StringUtils.isNotBlank(oldToken)){ //不为空，说明之前登录过
            token = oldToken;
        }
        //刚登录
        String newToken = request.getParameter("token"); //游览器地址栏
        if (StringUtils.isNotBlank(newToken)){
            token = newToken; //如果都新旧都不为空，反正new会覆盖old
        }

        /**
         * 已经拦截，是否必须要登录成功才可以访问功能
         */
        //是否必须登录
        boolean loginSuccess = methodAnnotation.loginSuccess();//获得该请求是否必须要登录成功才可以访问功能

        /**
         * 两种情况都需要验证，所以将验证提取
         */
        //验证：调用验证中心进行验证 、、verify方法
        String success = "fail";
            Map<String,String> successMap = new HashMap<>();
            if (StringUtils.isNotBlank(token)){
                String ip = request.getHeader("x-forwarded-for"); //通过Nginx转发的客服端ip
                if (StringUtils.isBlank(ip)){
                    ip = request.getRemoteAddr(); //从request中获取ip
                    if (StringUtils.isBlank(ip)){
                        ip = "127.0.0.1";
                    }
                }
                /**
                 * 用户请求携带它要访问的地址 ip ，
                 */
            String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token +"&currentIp=" +ip);

            successMap = JSON.parseObject(successJson, Map.class);

            success = successMap.get("status");
        }

        /**
         * 有那个注解，无论是否必须要登录，都要进行验证
         */
        if (loginSuccess){
            //必须登录成功才能使用
            if (!success.equals("success")){
                //踢回认证中心:重定向回passport登录
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://passport.gmall.com:8085/index?RetrunUrl="+requestURL);
                return  false;
            }else {
                //验证
                //验证通过，需要将token中携带的用户信息写入
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickName", successMap.get("nickName"));
                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)){
                    CookieUtil.addCookie(request,response,"oldToken",token,60*60*2);
                }
                return true;
            }
        }
        else {
            //不需登录也能可以使用，但是必须验证
            if (success.equals("success")){ //验证通过
                //需要将token中携带的用户信息写入
                request.setAttribute("memberId",successMap.get("memberId"));
                request.setAttribute("nickName", successMap.get("nickName"));

                //验证通过，覆盖cookie中的token
                if (StringUtils.isNotBlank(token)){
                    CookieUtil.addCookie(request,response,"oldToken",token,60*60*2);
                }
            }
//            return true;
        }

        return true;
    }
}

















