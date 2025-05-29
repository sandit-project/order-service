package com.example.orderservice.order.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSocialUidDTO {
    private Long userUid;
    private Long socialUid;
}
