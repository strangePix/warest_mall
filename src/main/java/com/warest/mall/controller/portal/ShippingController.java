package com.warest.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Shipping;
import com.warest.mall.domain.User;
import com.warest.mall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * 收货地址管理模块
 */
@RestController
@RequestMapping("/shipping")
public class ShippingController {


    @Autowired
    private IShippingService iShippingService;


    //todo  登录拦截器
    private Integer checkLoginId(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return user==null?null:user.getId();
    }

    /**
     * 新增地址
     * @param session
     * @param shipping  地址相关数据，对象关联
     * @return
     */
    @PostMapping("add")
    public ResponseEntity add(HttpSession session, Shipping shipping){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()): iShippingService.add(userId,shipping);
    }


    /**
     * 删除地址
     * @param session
     * @param shippingId  删除id
     * @return
     */
    @PostMapping("delete")
    public ResponseEntity del(HttpSession session, Integer shippingId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()): iShippingService.del(userId,shippingId);
    }

    
    @PostMapping("update")
    public ResponseEntity update(HttpSession session, Shipping shipping){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()): iShippingService.update(userId,shipping);
    }


    @PostMapping("select")
    public ResponseEntity<Shipping> select(HttpSession session, Integer shippingId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请登录之后查询"): iShippingService.select(userId,shippingId);
    }


    @PostMapping("list")
    public ResponseEntity<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                         HttpSession session){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()): iShippingService.list(userId,pageNum,pageSize);
    }














}
