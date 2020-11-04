package com.warest.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.User;
import com.warest.mall.service.IOrderService;
import com.warest.mall.service.IUserService;
import com.warest.mall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Created by geely
 */

@RestController
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @PostMapping("list")
    public ResponseEntity<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑
            return iOrderService.manageList(pageNum,pageSize);
        }else{
            return ResponseEntity.createByErrorMessage("无权限操作");
        }
    }

    @PostMapping("detail")
    public ResponseEntity<OrderVo> orderDetail(HttpSession session, Long orderNo){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑

            return iOrderService.manageDetail(orderNo);
        }else{
            return ResponseEntity.createByErrorMessage("无权限操作");
        }
    }



    @PostMapping("search")
    public ResponseEntity<PageInfo> orderSearch(HttpSession session, Long orderNo, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else{
            return ResponseEntity.createByErrorMessage("无权限操作");
        }
    }



    @PostMapping("send_goods")
    public ResponseEntity<String> orderSendGoods(HttpSession session, Long orderNo){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑
            return iOrderService.manageSendGoods(orderNo);
        }else{
            return ResponseEntity.createByErrorMessage("无权限操作");
        }
    }








}
