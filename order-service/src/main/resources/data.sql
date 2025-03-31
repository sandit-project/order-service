CREATE DATABASE IF NOT EXISTS `order`;
USE `order`;
CREATE TABLE IF NOT EXISTS `order` (
                                       uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       user_uid BIGINT,
                                       social_uid BIGINT,
                                        // 추가 여부 물어보기
                                        menu_uid BIGINT NOT NULL,
                                       menu_name VARCHAR(255) NOT NULL,
    amount INT NOT NULL,
    price BIGINT NOT NULL,
    calorie DOUBLE NOT NULL,
    payment VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reservation_date TIMESTAMP,
    version INT NOT NULL,
    FOREIGN KEY (user_uid) REFERENCES auth.user(uid),
    FOREIGN KEY (social_uid) REFERENCES auth.social(uid),
    //추가 여부 물어보기
    FOREIGN KEY (menu_uid) REFERENCES menu.menu(uid)
    );


CREATE TABLE IF NOT EXISTS `custom_order` (
                                              uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                              bread BIGINT NOT NULL,
                                              main_material_1 BIGINT NOT NULL,
                                              main_material_2 BIGINT,
                                              main_material_3 BIGINT,
                                              cheeze BIGINT,
                                              vegetable_1 BIGINT NOT NULL,
                                              vegetable_2 BIGINT,
                                              vegetable_3 BIGINT,
                                              vegetable_4 BIGINT,
                                              vegetable_5 BIGINT,
                                              vegetable_6 BIGINT,
                                              vegetable_7 BIGINT,
                                              vegetable_8 BIGINT,
                                              sauce_1 BIGINT NOT NULL,
                                              sauce_2 BIGINT,
                                              sauce_3 BIGINT,
                                              version INT NOT NULL,
                                              FOREIGN KEY (uid) REFERENCES `order`(uid),
    FOREIGN KEY (bread) REFERENCES menu.bread(uid),
    FOREIGN KEY (main_material_1) REFERENCES menu.main_material(uid),
    FOREIGN KEY (main_material_2) REFERENCES menu.main_material(uid),
    FOREIGN KEY (main_material_3) REFERENCES menu.main_material(uid),
    FOREIGN KEY (cheeze) REFERENCES menu.cheeze(uid),
    FOREIGN KEY (vegetable_1) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_2) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_3) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_4) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_5) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_6) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_7) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (vegetable_8) REFERENCES menu.vegetable(uid),
    FOREIGN KEY (sauce_1) REFERENCES menu.sauce(uid),
    FOREIGN KEY (sauce_2) REFERENCES menu.sauce(uid),
    FOREIGN KEY (sauce_3) REFERENCES menu.sauce(uid)
    );