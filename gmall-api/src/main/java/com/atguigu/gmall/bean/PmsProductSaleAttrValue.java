package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 商品销售属性值
 */
public class PmsProductSaleAttrValue implements Serializable {

    @Id
    @Column
    String id;

    @Column
    String productId;

    private String saleAttrId;

    @Transient
    String isCheckId;//属性值是否被选中

    private String name;
    private String value;

    public String getIsCheckId() {
        return isCheckId;
    }

    public void setIsCheckId(String isCheckId) {
        this.isCheckId = isCheckId;
    }

    public String getSaleAttrId() {
        return saleAttrId;
    }

    public void setSaleAttrId(String saleAttrId) {
        this.saleAttrId = saleAttrId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}




























