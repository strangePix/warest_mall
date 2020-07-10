package com.warest.mall.service.impl;

import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.common.TokenCache;
import com.warest.mall.dao.UserMapper;
import com.warest.mall.domain.User;
import com.warest.mall.service.IUserService;
import com.warest.mall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by geely
 */
@Service("iUserService")
//@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public ResponseEntity<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0 ){
            return ResponseEntity.createByErrorMessage("用户名不存在");
        }
        // 密码登录md5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user  = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ResponseEntity.createByErrorMessage("密码错误");
        }
        //上述情况没返回，说明登录成功，隐藏密码
        //user.setPassword(StringUtils.EMPTY);
        return ResponseEntity.createBySuccess("登录成功",user);
    }


    /**
     * 注册
     * @param user
     * @return
     */
    public ResponseEntity<String> register(User user){
        ResponseEntity validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ResponseEntity.createByErrorMessage("注册失败");
        }
        return ResponseEntity.createBySuccessMessage("注册成功");
    }

    /**
     * 检验用户名是否有效（username/email）
     * @param str
     * @param type
     * @return
     */
    public ResponseEntity<String> checkValid(String str, String type){
        if(StringUtils.isNotBlank(type)){  //判断不为空，这里包括null/空字符串/全空格
            //开始校验，先判断用户名
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0 ){
                    return ResponseEntity.createByErrorMessage("用户名已存在");
                }
            }
            //在判断email
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0 ){
                    return ResponseEntity.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ResponseEntity.createByErrorMessage("参数错误");
        }
        return ResponseEntity.createBySuccessMessage("校验成功");
    }

    /**
     * 根据用户名查询密码提示问题
     * @param username
     * @return
     */
    public ResponseEntity selectQuestion(String username){

        //检查用户是否存在，调用的同类方法，为1时才是用户名存在，0对应success但反而是不存在的
        ResponseEntity validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            return ResponseEntity.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        //检查问题是否为空（无意义）
        if(StringUtils.isNotBlank(question)){
            return ResponseEntity.createBySuccess(question);
        }
        return ResponseEntity.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 根据问题和回答判断是否是对应用户
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ResponseEntity<String> checkAnswer(String username, String question, String answer){
        //判断存在答案且答案正确
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明问题及问题答案是这个用户的,并且是正确的
            String forgetToken = UUID.randomUUID().toString();  //生成一个token
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);  //存起来，并设置有效期
            return ResponseEntity.createBySuccess(forgetToken);
        }
        return ResponseEntity.createByErrorMessage("问题的答案错误");
    }


    /**
     * 验证token，通过后修改密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ResponseEntity<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        //验证token是否提供
        if(StringUtils.isBlank(forgetToken)){
            return ResponseEntity.createByErrorMessage("参数错误,token需要传递");
        }
        //验证账号是否存在
        ResponseEntity validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ResponseEntity.createByErrorMessage("修改密码操作失效：用户不存在");
        }
        //从服务器取出账号token，如果当初没设就没有，或者过期
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ResponseEntity.createByErrorMessage("修改密码操作失效：token不存在/过期");
        }
        //判断token与提供的是否一致，一致则重设密码
        if(StringUtils.equals(forgetToken,token)){
            String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);

            if(rowCount > 0){
                return ResponseEntity.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ResponseEntity.createByErrorMessage("修改密码操作失效：token无效/过期");
        }
        return ResponseEntity.createByErrorMessage("修改密码操作失效");
    }


    public ResponseEntity<String> resetPassword(String passwordOld, String passwordNew, User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.
        // 因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ResponseEntity.createByErrorMessage("密码更新失败：原密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ResponseEntity.createBySuccessMessage("密码更新成功");
        }
        return ResponseEntity.createByErrorMessage("密码更新失败");
    }


    public ResponseEntity<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,
        //这里的查询语句是  email不存在或者email已存在但就是这个用户的时候才会返回0
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            //这种情况是email没变
            return ResponseEntity.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        //更新用户时  不包括用户名和密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            updateUser.setAnswer(null);
            return ResponseEntity.createBySuccess("更新个人信息成功",updateUser);
        }
        return ResponseEntity.createByErrorMessage("更新个人信息失败");
    }



    public ResponseEntity<User> getInformation(Integer userId){
//        User user = userMapper.selectByPrimaryKey(userId);
        User user = userMapper.getInformationBySession(userId);
        if(user == null){
            return ResponseEntity.createByErrorMessage("找不到当前用户");
        }
        //user.setPassword(StringUtils.EMPTY);
        return ResponseEntity.createBySuccess(user);

    }




    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ResponseEntity checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ResponseEntity.createBySuccess();
        }
        return ResponseEntity.createByError();
    }



}
