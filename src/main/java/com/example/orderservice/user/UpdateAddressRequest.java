package com.example.orderservice.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAddressRequest {
    private int userUid;
    private String mainAddress;
    private String subAddress1;
    private String subAddress2;
    private double mainLat;
    private double mainLan;
    private double subLat1;
    private double subLan1;
    private double subLat2;
    private double subLan2;
}
