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

    @Column(name = "regular_price")
    private String regularPrice;

    @Column(name = "move_in_out_price")
    private String moveInOutPrice;

    @Column(name = "deep_clean_price")
    private String deepCleanPrice;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "code")
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getDeepCleanPrice() {
        return deepCleanPrice;
    }

    public void setDeepCleanPrice(String deepCleanPrice) {
        this.deepCleanPrice = deepCleanPrice;
    }

    public String getMoveInOutPrice() {
        return moveInOutPrice;
    }

    public void setMoveInOutPrice(String moveInOutPrice) {
        this.moveInOutPrice = moveInOutPrice;
    }

    public String getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(String regularPrice) {
        this.regularPrice = regularPrice;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}