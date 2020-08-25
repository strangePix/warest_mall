package com.warest.mall.controller.backend;

import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Category;
import com.warest.mall.domain.User;
import com.warest.mall.service.ICategoryService;
import com.warest.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;


@RestController
@RequestMapping("/manage/category")
public class CategoryManageController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;


    //todo  登录拦截器
    private ResponseEntity<String> checkAdminLogin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "管理员未登录");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return null;
        } else {
            return ResponseEntity.createByErrorMessage("当前用户无权限，请联系管理员操作");
        }
    }

    /**
     * 增加节点
     *
     * @param session
     * @param categoryName
     * @param parentId     赋予默认值，如果前端不传则默认根节点
     * @return
     */
    @PostMapping("add_category")
    public ResponseEntity<String> addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return responseEntity;
        }
    }

    /**
     * 修改品类名称
     *
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @PostMapping("set_category_name")
    public ResponseEntity<String> setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //更新
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        } else {
            return responseEntity;
        }
    }

    /**
     * 获取品类子节点 不递归
     *
     * @param session
     * @param categoryId
     * @return
     */
    @PostMapping("get_category")
    public ResponseEntity<List<Category>> getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //查询子节点的category信息,并且不递归,保持平级
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return responseEntity;
        }


    }

    /**
     * 获取当前分类id及递归子节点id
     *
     * @param session
     * @param categoryId
     * @return
     */
    @PostMapping("get_deep_category")
    public ResponseEntity<List<Integer>> getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {

        ResponseEntity responseEntity = this.checkAdminLogin(session);
        if (responseEntity == null) {
            //查询当前节点的id和递归子节点的id
            //0->10000->100000
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return responseEntity;
        }
    }


}
