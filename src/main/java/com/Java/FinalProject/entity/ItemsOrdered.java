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
}
