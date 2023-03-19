package com.github.ygyin.entity;

public class GoodsStock {
    private Integer goodsId;
    private String goodsName;
    private Integer balance;
    private Integer sales;
    private Integer ver;

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getVer() {
        return ver;
    }

    public void setVer(Integer ver) {
        this.ver = ver;
    }

    @Override
    public String toString() {
        return "GoodsStock{" +
                "goodsId=" + goodsId +
                ", goodsName='" + goodsName + '\'' +
                ", stock=" + balance +
                ", sale=" + sales +
                ", ver=" + ver +
                '}';
    }
}
