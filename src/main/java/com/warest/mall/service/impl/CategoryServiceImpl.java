package com.warest.mall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.dao.CategoryMapper;
import com.warest.mall.domain.Category;
import com.warest.mall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by geely
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 在相应节点添加品类
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ResponseEntity<String> addCategory(String categoryName, Integer parentId) {
        // 参数校验
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ResponseEntity.createByErrorMessage("添加品类失败：参数为空");
        }
        if (parentId != 0 && categoryMapper.checkCategoryId(parentId) == 0) {
            return ResponseEntity.createByErrorMessage("添加品类失败：未找到该分类");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ResponseEntity.createBySuccess("添加品类成功");
        }
        return ResponseEntity.createByErrorMessage("添加品类失败：请重试");
    }

    /**
     * 更新品类名称
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ResponseEntity<String> updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ResponseEntity.createByErrorMessage("更新品类失败：参数为空");
        }
        if (categoryMapper.checkCategoryId(categoryId) == 0) {
            return ResponseEntity.createByErrorMessage("添加品类失败：未找到该分类");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ResponseEntity.createBySuccess("更新品类成功");
        }
        return ResponseEntity.createByErrorMessage("更新品类失败：请重试");
    }

    /**
     * 获取分类一级子分类
     *
     * @param categoryId
     * @return
     */
    public ResponseEntity<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        if (categoryId == null || (categoryId != 0 && categoryMapper.checkCategoryId(categoryId) == 0)) {
            return ResponseEntity.createByErrorMessage("未找到该分类");
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类");
        }
        return ResponseEntity.createBySuccess(categoryList);
    }


    /**
     * 递归查询本节点的id及孩子节点的id
     *
     * @param categoryId
     * @return
     */
    public ResponseEntity<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        if (categoryId == null || (categoryId != 0 && categoryMapper.checkCategoryId(categoryId) == 0)) {
            return ResponseEntity.createByErrorMessage("未找到该分类");
        }
        // 把set中的节点id取出放到list中
        List<Integer> categoryIdList = Lists.newArrayList();
        /*Set<Category> categorySet = findChildCategory(Sets.newHashSet(), categoryId);
        for (Category categoryItem : categorySet) {
            categoryIdList.add(categoryItem.getId());
        }*/
        categoryIdList.addAll(findChildCategory(categoryId));
        return ResponseEntity.createBySuccess(categoryIdList);
    }


    /**
     * 递归算法,算出子节点
     * (这个效率也太低了)
     *
     * @param categorySet 找出的结果存放地
     * @param categoryId  目标节点，找他的子节点
     * @return
     */
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        if (categorySet != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category != null) {
                categorySet.add(category);
            }
            //查找子节点,递归算法一定要有一个退出的条件
            List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
            for (Category categoryItem : categoryList) {
                findChildCategory(categorySet, categoryItem.getId());
            }
        }
        return categorySet;
    }

    /**
     * 循环实现  算0的时候不把0算进去
     * @param categoryId
     * @return
     */
    private Set<Integer> findChildCategory(Integer categoryId) {
        Set<Integer> categorySet = Sets.newHashSet();
        Stack<Integer> categoryStack = new Stack<Integer>();
        categoryStack.push(categoryId);
        if(categoryId!=0)categorySet.add(categoryId);
        while (!categoryStack.empty()) {
            List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryStack.pop());
            categoryList.stream().forEach(e->{
                categoryStack.push(e.getId());
                categorySet.add(e.getId());
            });
            // for (Category category : categoryList) {
            //     categoryId = category.getId();
            //     categoryStack.push(categoryId);
            //     categorySet.add(categoryId);
            // }
        }
        return categorySet;
    }


}
