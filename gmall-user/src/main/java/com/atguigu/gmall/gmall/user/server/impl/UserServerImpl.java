package com.atguigu.gmall.gmall.user.server.impl;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.payment.service.UserServer;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;

import java.util.List;

/**
 * dubbo的@Service
 */
@Service
public class UserServerImpl implements UserServer {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    /**
     * 查询所有的user
     * @return
     */
    @Override
    public List<UmsMember> getAllUser() {

        /**
         * 用了通用mapper之后，就可以不写 xml文件的sql语句了
         *                   也可以不在UserMapper中写方法了
         */
        /**
         * 如果用通用mapper，就是userMapper.selectUser()
         */
        List<UmsMember> umsMemberList = userMapper.selectAllUser();  //userMapper.selectAllUser();
        return umsMemberList;
    }

    /**
     * 根据外键查询：根据memberId查询用户收货地址集合
     * @param memberId
     * @return
     */
    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        //封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        //根据memberId查询用户收货地址集合
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);


//        查询方式2
//        Example example = new Example(UmsMemberReceiveAddress.class);
//        example.createCriteria().andEqualTo("memberId",memberId);
//        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(example);//根据外键查询


        return umsMemberReceiveAddresses;
    }

    /**
     *根据UmsMember表主键查询
     * @param id
     * @return
     */
    @Override
    public List<UmsMember> getUmsMembersById(String id) {

        UmsMember umsMember = new UmsMember();
        umsMember.setId(id);

        List<UmsMember> umsMembers = userMapper.select(umsMember);
        return umsMembers;
    }


}























