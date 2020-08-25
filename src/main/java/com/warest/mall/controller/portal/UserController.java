package com.warest.mall.controller.portal;

import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.User;
import com.warest.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;


@RestController   //节省了@ResponseBody和@Controller
@RequestMapping("/user")
public class UserController {


    @Autowired
    private IUserService iUserService;


    /**
     * 用户登录
     * 通过插件将返回的对象转化为json
     * @param username
     * @param password
     * @param session
     * @return
     */
    @PostMapping(value = "login")
    public ResponseEntity<User> login(String username, String password, HttpSession session){
        ResponseEntity<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
//            登录成功的用户存到session中
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @PostMapping(value = "logout")
    public ResponseEntity<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return ResponseEntity.createBySuccessMessage("退出成功");
        //todo 登出失败的情况  报服务端异常，但暂时没想到失败的情况
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping(value = "register")
    public ResponseEntity<String> register(User user){
        return iUserService.register(user);
    }


    /**
     * 校验接口 根据用户名/email进行校验
     * @param str
     * @param type
     * @return
     */
    @PostMapping(value = "check_valid")
    public ResponseEntity<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }


    /**
     * 获取登录用户信息，根据session
     * @param session
     * @return
     */
    @PostMapping(value = "get_user_info")
    public ResponseEntity<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ResponseEntity.createBySuccess(user);
        }
        return ResponseEntity.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }


    /**
     * 点击忘记密码的返回，提供当初设定的密码提示问题
     * @param username 用户名
     * @return
     */
    @PostMapping(value = "forget_get_question")
    //@ResponseBody
    public ResponseEntity<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }


    /**
     * 提交问题答案
     * @param username
     * @param question
     * @param answer
     * @return  正确的返回包含一个token，用于修改密码，需要传回
     */
    @PostMapping(value = "forget_check_answer")
    //@ResponseBody
    public ResponseEntity<String> forgetCheckAnswer(String username, String question, String answer){
        return iUserService.checkAnswer(username,question,answer);
    }


    /**
     * 忘记密码，重设密码（未登录状态）
     * @param username
     * @param passwordNew
     * @param forgetToken  提供找回密码的token，通过密码问题回答获取
     * @return
     */
    @PostMapping(value = "forget_reset_password")
    //@ResponseBody
    public ResponseEntity<String> forgetRestPassword(String username, String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }


    /**
     * 登录状态重设密码
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @PostMapping(value = "reset_password")
    //@ResponseBody
    public ResponseEntity<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ResponseEntity.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }


    /**
     * 登录状态更新个人信息
     * username不能修改，email也不能修改为已存在的地址
     * @param session
     * @param user
     * @return
     */
    @PostMapping(value = "update_information")
    //@ResponseBody
    public ResponseEntity<User> update_information(HttpSession session, User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ResponseEntity.createByErrorMessage("用户未登录");
        }
        String username = currentUser.getUsername();
        Integer role = currentUser.getRole();
        user.setId(currentUser.getId());
        user.setUsername(username);
        ResponseEntity<User> response = iUserService.updateInformation(user);
        if(response.isSuccess() && response.getData()!=null){
            currentUser = response.getData();
            currentUser.setUsername(username);
            currentUser.setRole(role);
            session.setAttribute(Const.CURRENT_USER,currentUser);
            return ResponseEntity.createBySuccessMessage(response.getMsg());
        }
        //todo response.getData()为空但成功的情况没有处理
        return response;
    }

    /**
     * 获取当前登录用户的详细信息，并强制登录(通过session)
     * @param session
     * @return
     */
    @PostMapping(value = "get_information")
    //@ResponseBody
    public ResponseEntity<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,无法获取当前用户信息,需要强制登录,status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }






























}
