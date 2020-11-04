package com.warest.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.dao.ShippingMapper;
import com.warest.mall.domain.Shipping;
import com.warest.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 收货地址管理
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {


    @Autowired
    private ShippingMapper shippingMapper;

    public ResponseEntity add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        //需要及时获取生成的主键id  在xml配置修改
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ResponseEntity.createBySuccess("新建地址成功",result);
        }
        return ResponseEntity.createByErrorMessage("新建地址失败");
    }

    public ResponseEntity<String> del(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return ResponseEntity.createBySuccess("删除地址成功");
        }
        return ResponseEntity.createByErrorMessage("删除地址失败");
    }


    public ResponseEntity update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return ResponseEntity.createBySuccess("更新地址成功");
        }
        return ResponseEntity.createByErrorMessage("更新地址失败");
    }

    public ResponseEntity<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping == null){
            return ResponseEntity.createByErrorMessage("无法查询到该地址");
        }
        return ResponseEntity.createBySuccess(shipping);
    }

    /**
     * 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ResponseEntity<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ResponseEntity.createBySuccess(pageInfo);
    }







}
