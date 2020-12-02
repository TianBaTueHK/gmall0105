package com.atguigu.gmall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

public class PmsBaseCatalog3 implements Serializable {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String catalog2Id;

    @Column
    private String name;

    @Transient
    private String catalog2s;

    @Override
    public String toString() {
        return "PmsBaseCatalog3{" +
                "id='" + id + '\'' +
                ", catalog2Id='" + catalog2Id + '\'' +
                ", name='" + name + '\'' +
                ", catalog2s='" + catalog2s + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCatalog2Id() {
        return catalog2Id;
    }

    public void setCatalog2Id(String catalog2Id) {
        this.catalog2Id = catalog2Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog2s() {
        return catalog2s;
    }

    public void setCatalog2s(String catalog2s) {
        this.catalog2s = catalog2s;
    }
}

































