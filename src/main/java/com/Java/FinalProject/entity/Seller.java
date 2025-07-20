package com.Java.FinalProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sellerId;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Column(name = "seller_email", nullable = false, unique = true)
    private String sellerEmail;

    @Column(name = "seller_password", nullable = false)
    private String sellerPassword;

}
