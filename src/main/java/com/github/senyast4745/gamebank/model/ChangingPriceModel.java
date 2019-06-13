package com.github.senyast4745.gamebank.model;

import javax.persistence.*;

@Entity
@Table(name = "changing_price")
public class ChangingPriceModel {

    @Id
    @GeneratedValue
    @Column(name = "changing_price_id")
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "stock_price")
    private Double stockPrice;

    @Column(name = "price_time")
    private String priceTime;

    public ChangingPriceModel() {
    }

    public ChangingPriceModel(Long companyId, Double stockPrice, String priceTime) {
        this.companyId = companyId;
        this.stockPrice = stockPrice;
        this.priceTime = priceTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setStockPrice(Double stockPrice) {
        this.stockPrice = stockPrice;
    }

    public void setPriceTime(String priceTime) {
        this.priceTime = priceTime;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Double getStockPrice() {
        return stockPrice;
    }

    public String getPriceTime() {
        return priceTime;
    }
}

