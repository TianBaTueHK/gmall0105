package com.atguigu.gmall.cart.controller;

import java.math.BigDecimal;

/**
 * 的价格计算
 */
public class TestBigDecimal {

    public static void main(String[] args) {


        //初始化
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        BigDecimal b3 = new BigDecimal("0.01");

        //比较
        int i = b1.compareTo(b2);
        System.out.println(i);

        //运算
        BigDecimal add = b1.add(b3);
        System.out.println(add);

        //约数

    }
}



























