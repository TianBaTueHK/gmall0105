package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * 商品销售属性
 */
public class PmsProductSaleAttr implements Serializable {

    @Id
    @Column
    private String id;

    @Column
    String productId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrName;

    List<PmsProductSaleAttr> SpuSaleAttrValueList;

    @Transient
    List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PmsProductSaleAttr> getSpuSaleAttrValueList() {
        return SpuSaleAttrValueList;
    }

    public void setSpuSaleAttrValueList(List<PmsProductSaleAttr> spuSaleAttrValueList) {
        SpuSaleAttrValueList = spuSaleAttrValueList;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSaleAttrId() {
        return saleAttrId;
    }

    public void setSaleAttrId(String saleAttrId) {
        this.saleAttrId = saleAttrId;
    }

    public String getSaleAttrName() {
        return saleAttrName;
    }

    public void setSaleAttrName(String saleAttrName) {
        this.saleAttrName = saleAttrName;
    }

    public List<PmsProductSaleAttrValue> getPmsProductSaleAttrValueList() {
        return pmsProductSaleAttrValueList;
    }

    public void setPmsProductSaleAttrValueList(List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList) {
        this.pmsProductSaleAttrValueList = pmsProductSaleAttrValueList;
    }


//    public void setSpuSaleAttrValueList(List<PmsProductSaleAttrValue> pmsProductSaleAttrValues) {
//
//    }
}










































