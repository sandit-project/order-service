package com.example.orderservice.dummy;

import com.example.orderservice.menu.Menu;
import com.example.orderservice.menu.MenuClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//더미 데이터
@Component
public class MenuClientFallback implements MenuClient {

    @Override
    public Menu getMenuByUid(@PathVariable("uid") Integer uid) {
        Menu dummy = Menu.builder()
                .uid(2)
                .menuName("햄치즈 샌드위치")
                .price(2)
                .calorie(1.1)
                .bread(1)
                .material1(1)
                .material2(1)
                .build();
        return dummy;
    }

    @Override
    public List<Menu> getMenus() {
        return List.of();
    }

    @Override
    public Menu createMenu(Menu menu) {
        return null;
    }

    @Override
    public Menu updateMenu(Integer uid, Menu menu) {
        return null;
    }

    @Override
    public void deleteMenu(Integer uid) {

    }
}
