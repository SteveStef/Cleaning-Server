package com.mainlineclean.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class AdminDetails {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "regular")
    private String regularPrice;

    @Column(name = "move_in_out")
    private String moveInOutPrice;

    @Column(name = "environment")
    private String environmentPrice;

    @Column(name = "fire")
    private String firePrice;

    @Column(name = "water")
    private String waterPrice;

    @Column(name = "deceased")
    private String deceasedPrice;

    @Column(name = "hazmat")
    private String hazmat;

    @Column(name = "explosive_residue")
    private String explosiveResidue;

    @Column(name = "mold")
    private String moldPrice;

    @Column(name = "construction")
    private String constructionPrice;

    @Column(name = "commercial")
    private String commercialPrice;

    @Column(name = "deep")
    private String deepCleanPrice;

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

    public String getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(String regularPrice) {
        this.regularPrice = regularPrice;
    }

    public String getEnvironmentPrice() {
        return environmentPrice;
    }

    public void setEnvironmentPrice(String environmentPrice) {
        this.environmentPrice = environmentPrice;
    }

    public String getFirePrice() {
        return firePrice;
    }

    public void setFirePrice(String firePrice) {
        this.firePrice = firePrice;
    }

    public String getWaterPrice() {
        return waterPrice;
    }

    public void setWaterPrice(String waterPrice) {
        this.waterPrice = waterPrice;
    }

    public String getDeceasedPrice() {
        return deceasedPrice;
    }

    public void setDeceasedPrice(String deceasedPrice) {
        this.deceasedPrice = deceasedPrice;
    }

    public String getHazmat() {
        return hazmat;
    }

    public void setHazmat(String hazmat) {
        this.hazmat = hazmat;
    }

    public String getExplosiveResidue() {
        return explosiveResidue;
    }

    public void setExplosiveResidue(String explosiveResidue) {
        this.explosiveResidue = explosiveResidue;
    }

    public String getMoldPrice() {
        return moldPrice;
    }

    public void setMoldPrice(String moldPrice) {
        this.moldPrice = moldPrice;
    }

    public String getConstructionPrice() {
        return constructionPrice;
    }

    public void setConstructionPrice(String constructionPrice) {
        this.constructionPrice = constructionPrice;
    }

    public String getCommercialPrice() {
        return commercialPrice;
    }

    public void setCommercialPrice(String commercialPrice) {
        this.commercialPrice = commercialPrice;
    }

    public String getDeepCleanPrice() {
        return deepCleanPrice;
    }

    public void setDeepCleanPrice(String deepCleanPrice) {
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

    public void setMoveInOutPrice(String moveInOutPrice) {
        this.moveInOutPrice = moveInOutPrice;
    }

    public String getMoveInOutPrice() {
        return moveInOutPrice;
    }
}