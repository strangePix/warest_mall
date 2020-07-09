package com.warest.mall.service;

import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.User;

/**
 * Created by geely
 */
public interface IUserService {

    ResponseEntity<User> login(String username, String password);

    ResponseEntity<String> register(User user);

    ResponseEntity<String> checkValid(String str, String type);

    ResponseEntity selectQuestion(String username);

    ResponseEntity<String> checkAnswer(String username, String question, String answer);

    ResponseEntity<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ResponseEntity<String> resetPassword(String passwordOld, String passwordNew, User user);

    ResponseEntity<User> updateInformation(User user);

    ResponseEntity<User> getInformation(Integer userId);

    ResponseEntity checkAdminRole(User user);
}
