package com.warest.mall.vo;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品vo
 */
// @Component
public class ProductDetailVo {

    private Integer id;
    private Integer categoryId;
    private String name;
    // 副标题
    private String subtitle;
    //主图
    private String mainImage;
    // 子图
    private String subImages;
    // 详情描述
    private String detail;
    // 价格
    private BigDecimal price;
    // 库存
    private Integer stock;
    // 状态
    private Integer status;

    // 时间戳？
    // private String createTime;
    // private String updateTime;
    private Date createTime;
    private Date updateTime;

    // 图片服务器url与图片文件名拼接为完整的
    @Value("${ftp.server.http.prefix:http://img.happymmall.com/}")
    private String imageHost;
    // 父分类id
    private Integer parentCategoryId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getSubImages() {
        return subImages;
    }

    public void setSubImages(String subImages) {
        this.subImages = subImages;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}
