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
         Integer mainMaterial1,
         Integer mainMaterial2,
         Integer mainMaterial3,
         Integer cheeze,
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
         File image,
         String status
){
}
