package com.warest.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.service.IProductService;
import com.warest.mall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台商品功能
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService iProductService;


    /**
     * 获取商品详情
     * 与后台的区别是商品状态非上线时也不显示
     * @param productId
     * @return
     */
    @PostMapping("detail")
    // @ResponseBody
    public ResponseEntity<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    /**
     * 搜索
     * @param keyword 关键字 非必须
     * @param categoryId 分类id 非必须
     * @param pageNum 页码
     * @param pageSize 每页数
     * @param orderBy 排序 默认不排序  两种排序值 price_desc price_asc  按照价格排序
     * @return
     */
    @PostMapping("list")
    // @ResponseBody
    public ResponseEntity<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return iProductService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }





}
