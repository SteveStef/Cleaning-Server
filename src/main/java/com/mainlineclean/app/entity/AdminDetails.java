package com.mainlineclean.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "admin")
public class AdminDetails {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "regular", precision = 19, scale = 2)
    private BigDecimal regularPrice;

    @Column(name = "move_in_out", precision = 19, scale = 2)
    private BigDecimal moveInOutPrice;

    @Column(name = "environment", precision = 19, scale = 2)
    private BigDecimal environmentPrice;

    @Column(name = "fire", precision = 19, scale = 2)
    private BigDecimal firePrice;

    @Column(name = "water", precision = 19, scale = 2)
    private BigDecimal waterPrice;

    @Column(name = "deceased", precision = 19, scale = 2)
    private BigDecimal deceasedPrice;

    @Column(name = "hazmat", precision = 19, scale = 2)
    private BigDecimal hazmatPrice;

    @Column(name = "explosive_residue", precision = 19, scale = 2)
    private BigDecimal explosiveResiduePrice;

    @Column(name = "mold", precision = 19, scale = 2)
    private BigDecimal moldPrice;

    @Column(name = "construction", precision = 19, scale = 2)
    private BigDecimal constructionPrice;

    @Column(name = "commercial", precision = 19, scale = 2)
    private BigDecimal commercialPrice;

    @Column(name = "deep", precision = 19, scale = 2)
    private BigDecimal deepCleanPrice;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "code")
    private String code;

    public AdminDetails() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(BigDecimal regularPrice) {
        this.regularPrice = regularPrice;
    }

    public BigDecimal getMoveInOutPrice() {
        return moveInOutPrice;
    }

    public void setMoveInOutPrice(BigDecimal moveInOutPrice) {
        this.moveInOutPrice = moveInOutPrice;
    }

    public BigDecimal getEnvironmentPrice() {
        return environmentPrice;
    }

    public void setEnvironmentPrice(BigDecimal environmentPrice) {
        this.environmentPrice = environmentPrice;
    }

    public BigDecimal getFirePrice() {
        return firePrice;
    }

    public void setFirePrice(BigDecimal firePrice) {
        this.firePrice = firePrice;
    }

    public BigDecimal getWaterPrice() {
        return waterPrice;
    }

    public void setWaterPrice(BigDecimal waterPrice) {
        this.waterPrice = waterPrice;
    }

    public BigDecimal getDeceasedPrice() {
        return deceasedPrice;
    }

    public void setDeceasedPrice(BigDecimal deceasedPrice) {
        this.deceasedPrice = deceasedPrice;
    }

    public BigDecimal getHazmatPrice() {
        return hazmatPrice;
    }

    public void setHazmatPrice(BigDecimal hazmatPrice) {
        this.hazmatPrice = hazmatPrice;
    }

    public BigDecimal getExplosiveResiduePrice() {
        return explosiveResiduePrice;
    }

    public void setExplosiveResiduePrice(BigDecimal explosiveResiduePrice) {
        this.explosiveResiduePrice = explosiveResiduePrice;
    }

    public BigDecimal getMoldPrice() {
        return moldPrice;
    }

    public void setMoldPrice(BigDecimal moldPrice) {
        this.moldPrice = moldPrice;
    }

    public BigDecimal getConstructionPrice() {
        return constructionPrice;
    }

    public void setConstructionPrice(BigDecimal constructionPrice) {
        this.constructionPrice = constructionPrice;
    }

    public BigDecimal getCommercialPrice() {
        return commercialPrice;
    }

    public void setCommercialPrice(BigDecimal commercialPrice) {
        this.commercialPrice = commercialPrice;
    }

    public BigDecimal getDeepCleanPrice() {
        return deepCleanPrice;
    }

    public void setDeepCleanPrice(BigDecimal deepCleanPrice) {
        this.deepCleanPrice = deepCleanPrice;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}