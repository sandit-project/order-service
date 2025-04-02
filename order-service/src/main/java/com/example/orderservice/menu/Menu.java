package com.example.orderservice.menu;

import lombok.Builder;

import java.io.File;

@Builder
public record Menu (
         Integer uid,
         String menuName,
         Integer price,
         Double calorie,
         Integer bread,
         Integer material1,
         Integer material2,
         Integer material3,
         Integer cheese,
         Integer vegetable1,
         Integer vegetable2,
         Integer vegetable3,
         Integer vegetable4,
         Integer vegetable5,
         Integer vegetable6,
         Integer vegetable7,
         Integer vegetable8,
         Integer sauce1,
         Integer sauce2,
         Integer sauce3,
         String image,
         String status
){
}
