package com.mainlineclean.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
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

    @Column(name = "custom", precision = 19, scale = 2)
    private BigDecimal customPrice;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "code")
    private String code;

    public AdminDetails() {}
}