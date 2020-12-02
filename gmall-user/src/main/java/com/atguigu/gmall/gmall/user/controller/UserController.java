package com.atguigu.gmall.gmall.user.controller;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.payment.service.UserServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 控制层
 */
@Controller
public class UserController {

    @Autowired
    UserServer userServer;

    /**
     * 根据UmsMemberReceiveAddress表外键查询
     * @param memberId
     * @return
     */
    @RequestMapping("getReceiveAddressByMemberId")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(@RequestBody String memberId){

        List<UmsMemberReceiveAddress> umsMemberReceiveAddress = userServer.getReceiveAddressByMemberId(memberId);
        return umsMemberReceiveAddress;
    }

//    --------------------------------------------------------------------------------------
//    -----------------------------------------------------------------------------------


    /**
     * 对UmsMember表进行更新
     * @param id
     * @return
     */
    @RequestMapping("updateMembersById")
    @ResponseBody
    public String updateMembersById(@RequestBody String id){

        return null;
    }

    /**
     * 根据UmsMember表主键查询
     * @param Id
     * @return
     */
    @RequestMapping("getUmsMembersById")
    @ResponseBody
    public List<UmsMember> getUmsMembersById(@RequestBody String Id){
        List<UmsMember>  umsMembers = userServer.getUmsMembersById(Id);

        return umsMembers;
    }

    /**
     * UmsMember表查询所有的user
     * @return
     */
    @RequestMapping("getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){

        List<UmsMember> umsMembers = userServer.getAllUser();
        return umsMembers;
    }


    /**
     * 测试环境配置是否正确
     * @return
     */
    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return "hello user";
    }



}





















