package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务层
 */
@Service
public interface UserServer {


    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);


    List<UmsMember> getUmsMembersById(String Id);

    UmsMember login(UmsMember umsMember);

    void addToken(String token, String memberId);

    public UmsMember addOauthUser(UmsMember umsMember);

    UmsMember checkOauthUser(UmsMember umsCheck);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}




















