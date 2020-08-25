package com.warest.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.dao.CategoryMapper;
import com.warest.mall.dao.ProductMapper;
import com.warest.mall.domain.Category;
import com.warest.mall.domain.Product;
import com.warest.mall.service.ICategoryService;
import com.warest.mall.service.IProductService;
import com.warest.mall.vo.ProductDetailVo;
import com.warest.mall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by geely
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {


    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;


    /**
     * 新增/更新产品
     * @param product
     * @return
     */
    public ResponseEntity saveOrUpdateProduct(Product product){
        if(product != null)
        {
            // 判断子图是否存在，是否多个，转换格式
            if(StringUtils.isNotBlank(product.getSubImages())){
                // 如果有，取第一个子图作为主图
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);
                }
            }

            if(product.getId() != null){
                int rowCount = productMapper.updateByPrimaryKeySelective(product);
                if(rowCount > 0){
                    return ResponseEntity.createBySuccess("更新产品成功");
                }
                return ResponseEntity.createBySuccess("更新产品失败");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount > 0){
                    return ResponseEntity.createBySuccess("新增产品成功");
                }
                return ResponseEntity.createBySuccess("新增产品失败");
            }
        }
        return ResponseEntity.createByErrorMessage("新增或更新产品失败：参数不正确");
    }


    /**
     * 产品上下架
     * @param productId
     * @param status 目前1是上架  1在售2下架3删除
     * @return
     */
    public ResponseEntity<String> setSaleStatus(Integer productId, Integer status){
        if(productId == null || status == null ){
            //非法参数
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        boolean legal = false;
        for (Const.ProductStatusEnum value : Const.ProductStatusEnum.values()) {
            if(status == value.getCode()){
                legal = true;
                break;
            }
        }
        if(!legal)return ResponseEntity.createByErrorMessage("修改产品销售状态失败");
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ResponseEntity.createBySuccess("修改产品销售状态成功");
        }
        return ResponseEntity.createByErrorMessage("修改产品销售状态失败");
    }


    /**
     * 获取产品详情 后台版
     * @param productId
     * @return
     */
    public ResponseEntity<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ResponseEntity.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ResponseEntity.createBySuccess(productDetailVo);
    }

    /**
     * 获取商品详情  前台版
     * 区别是查询出的产品信息会判断在售状态，查到但是下架状态也会返回不同结果
     * 产品状态作为枚举声明在Const类中
     * @param productId
     * @return
     */
    public ResponseEntity<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ResponseEntity.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ResponseEntity.createBySuccess(productDetailVo);
    }

    // 根据商品对象拼装vo
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        // productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.alienwarest.com.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        // productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setCreateTime(product.getCreateTime());
        // productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        productDetailVo.setUpdateTime(product.getUpdateTime());
        return productDetailVo;
    }


    /**
     * 分页查询商品列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ResponseEntity<PageInfo> getProductList(int pageNum, int pageSize){
        //startPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
        PageHelper.startPage(pageNum,pageSize,"id asc");
        //根据id升序查询
        List<Product> productList = productMapper.selectList();

        // 不需要product对象那么多属性，提供一个vo
        List<ProductListVo> productListVoList = Lists.newArrayList();
        // for(Product productItem : productList){
        //     ProductListVo productListVo = assembleProductListVo(productItem);
        //     productListVoList.add(productListVo);
        // }
        productList.stream().forEach(e->{
            productListVoList.add(assembleProductListVo(e));
        });
        // 不能直接放vo列表，而是放经过代理的查询结果，否则无法添加分页信息
        PageInfo pageResult = new PageInfo(productList);
        // 分页信息保留，但显示内容转换为vo
        pageResult.setList(productListVoList);
        return ResponseEntity.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        // productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


    /**
     * 根据id、商品名模糊查询
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ResponseEntity<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        // for(Product productItem : productList){
        //     ProductListVo productListVo = assembleProductListVo(productItem);
        //     productListVoList.add(productListVo);
        // }
        productList.stream().forEach(e->{
            productListVoList.add(assembleProductListVo(e));
        });
        PageInfo pageResult = new PageInfo(productList);
        // 分页信息保留，但显示内容转换为vo
        pageResult.setList(productListVoList);
        return ResponseEntity.createBySuccess(pageResult);
    }


    /**
     * 前台搜索返回结果 带分页
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    public ResponseEntity<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = Lists.newArrayList();

        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList); //这次因为是个空集合，没有与dao层互动，所以不需要传递再修改list
                return ResponseEntity.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData(); //这个效率很低，如果用在频繁查询感觉需要优化
        }
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        //排序处理  目前只有价格排序
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_"); //“price”“asc”
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        //查询防止空字符串和空集合  分页也在这里
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);

        List<ProductListVo> productVoList = Lists.newArrayList();
        productList.stream().forEach(e->{
            productVoList.add(assembleProductListVo(e));
        });

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productVoList);
        return ResponseEntity.createBySuccess(pageInfo);
    }


























}
