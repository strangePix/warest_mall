package com.warest.mall.service;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Product;
import com.warest.mall.vo.ProductDetailVo;

/**
 * Created by geely
 */
public interface IProductService {

    ResponseEntity saveOrUpdateProduct(Product product);

    ResponseEntity<String> setSaleStatus(Integer productId, Integer status);

    ResponseEntity<ProductDetailVo> manageProductDetail(Integer productId);

    ResponseEntity<PageInfo> getProductList(int pageNum, int pageSize);

    ResponseEntity<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    ResponseEntity<ProductDetailVo> getProductDetail(Integer productId);

    ResponseEntity<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy);



}
