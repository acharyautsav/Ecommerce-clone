package com.Java.FinalProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ItemsOrdered {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemsOrderedId;

    private Integer itemsOrderedQuantity;
    private Double itemsOrderedPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private CustomerOrder customerOrder;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
