package com.example.orderservice.order.domain;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("custom_order")
public record CustomOrder(
        @Id
        Integer uid,
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
        @Version
        int version

        ) {

        //CustomOrderRequestDTO → CustomOrder 변환용 유틸
        public static CustomOrder from(CustomOrderRequestDTO dto) {
                return CustomOrder.builder()
                        .uid(null)                    // 옵션만 저장할 땐 PK 미정
                        .bread(dto.getBread())
                        .material1(dto.getMaterial1())
                        .material2(dto.getMaterial2())
                        .material3(dto.getMaterial3())
                        .cheese(dto.getCheese())
                        .vegetable1(dto.getVegetable1())
                        .vegetable2(dto.getVegetable2())
                        .vegetable3(dto.getVegetable3())
                        .vegetable4(dto.getVegetable4())
                        .vegetable5(dto.getVegetable5())
                        .vegetable6(dto.getVegetable6())
                        .vegetable7(dto.getVegetable7())
                        .vegetable8(dto.getVegetable8())
                        .sauce1(dto.getSauce1())
                        .sauce2(dto.getSauce2())
                        .sauce3(dto.getSauce3())
                        .version(0)                   // 새로 저장하는 옵션이니 버전은 0
                        .build();
        }
}
