package com.atguigu.gmall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

public class PmsBaseCatalog2 implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String catalog1Id;

    @Column
    private String name;

    @Transient
    private List<PmsBaseCatalog3> baseCatalog3s;


    @Override
    public String toString() {
        return "PmsBaseCatalog2{" +
                "id='" + id + '\'' +
                ", catalog1Id='" + catalog1Id + '\'' +
                ", name='" + name + '\'' +
                ", baseCatalog3s=" + baseCatalog3s +
                '}';
    }

    public String getCatalog1Id() {
        return catalog1Id;
    }

    public void setCatalog1Id(String catalog1Id) {
        this.catalog1Id = catalog1Id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PmsBaseCatalog3> getBaseCatalog3s() {
        return baseCatalog3s;
    }

    public void setBaseCatalog3s(List<PmsBaseCatalog3> baseCatalog3s) {
        this.baseCatalog3s = baseCatalog3s;
    }
}

































