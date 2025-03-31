package com.example.orderservice.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("custom_order")
public record CustomOrder(
        @Id
        @Column("uid")
        Integer uid,
        Integer bread,
        @Column("main_material_1")
        Integer mainMaterial1,
        @Column("main_material_2")
        Integer mainMaterial2,
        @Column("main_material_3")
        Integer mainMaterial3,
        Integer cheeze,
        @Column("vegetable_1")
        Integer vegetable1,
        @Column("vegetable_2")
        Integer vegetable2,
        @Column("vegetable_3")
        Integer vegetable3,
        @Column("vegetable_4")
        Integer vegetable4,
        @Column("vegetable_5")
        Integer vegetable5,
        @Column("vegetable_6")
        Integer vegetable6,
        @Column("vegetable_7")
        Integer vegetable7,
        @Column("vegetable_8")
        Integer vegetable8,
        @Column("sauce_1")
        Integer sauce1,
        @Column("sauce_2")
        Integer sauce2,
        @Column("sauce_3")
        Integer sauce3

        ) {
}
