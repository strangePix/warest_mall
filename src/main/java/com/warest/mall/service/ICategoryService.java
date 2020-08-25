package com.warest.mall.service;

import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Category;

import java.util.List;

/**
 * Created by geely
 */
public interface ICategoryService {
    ResponseEntity addCategory(String categoryName, Integer parentId);

    ResponseEntity updateCategoryName(Integer categoryId, String categoryName);

    ResponseEntity<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ResponseEntity<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);

}
