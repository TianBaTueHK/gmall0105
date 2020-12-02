package com.atguigu.gamll.util;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * 对Cookie的封装，可以进行增删改查
 */
public class CookieUtil {


    /**
     * 获取所有Cookie
     * @param request
     * @return
     */
    public static Cookie[] getCookies(HttpServletRequest request) {
        return request.getCookies();
    }

    /**
     * 根据名称获取指定Cookie
     * @param request
     * @param name
     * @return
     */
    public static Cookie getCookieByName(HttpServletRequest request, String name,boolean isDecoder) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Cookie[] cookies = getCookies(request);
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 添加Cookie
     * @param response
     * @param name
     * @param value
     * @param maxAge
     * @return
     */
    public static boolean addCookie(HttpServletRequest request,HttpServletResponse response, String name, String value, int maxAge) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
            return false;
        }
        Cookie cookie = new Cookie(name.trim(), value.trim());
        if (maxAge <= 0) {
            maxAge = Integer.MAX_VALUE;
        }
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
        return true;
    }

    /**
     * 删除Cookie
     * 删除cookie，把value设置为null，把max-age设置为0就行了
     * @param request
     * @param response
     * @param name
     * @return
     */
    public static boolean removeCookie(HttpServletRequest request, HttpServletResponse response,
                                       String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        Cookie[] cookies = getCookies(request);
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    cookie.setValue(null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    return true;
                }

            }
        }
        return false;
    }

    public static void getCookieByName(HttpServletRequest request, HttpServletResponse response, String cartListCookie, String toJSONString, int i, boolean b) {

    }

    /**
     * 修改cookie
     * @param request
     * @param response
     * @param name
     * @param value
     * 注意一、修改、删除Cookie时，新建的Cookie除value、maxAge之外的所有属性，
     * 例如name、path、domain等，都要与原Cookie完全一样。否则，浏览器将视为两个不同的Cookie不予覆盖，导致修改、删除失败。
     */
    @RequestMapping("/editCookie")
    public void editCookie(HttpServletRequest request,HttpServletResponse response,String name,String value){
        Cookie[] cookies = request.getCookies();
        if(null==cookies) {
            System.out.println("没有cookie==============");
        }else{
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    System.out.println("原值为:"+cookie.getValue());
                    cookie.setValue(value);
                    cookie.setPath("/");
                    cookie.setMaxAge(30* 60);// 设置为30min
                    System.out.println("被修改的cookie名字为:"+cookie.getName()+",新值为:"+cookie.getValue());
                    response.addCookie(cookie);
                    break;
                }
            }
        }

    }
}












