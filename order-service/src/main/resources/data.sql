CREATE DATABASE IF NOT EXISTS `auth`;
USE `auth`;
CREATE TABLE IF NOT EXISTS `user` (
                                      uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                      user_id VARCHAR(255) NOT NULL UNIQUE,
                                      password VARCHAR(255) NOT NULL,
                                      user_name VARCHAR(255) NOT NULL,
                                      email VARCHAR(255) NOT NULL,
                                      emailyn CHAR(1) DEFAULT 'n',
                                      phone VARCHAR(255) NOT NULL,
                                      phoneyn CHAR(1) DEFAULT 'n',
                                      main_address VARCHAR(255) NOT NULL,
                                      sub_address_1 VARCHAR(255),
                                      sub_address_2 VARCHAR(255),
                                      point INT DEFAULT 0,
                                      status VARCHAR(20) DEFAULT 'active',
                                      created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      version INT NOT NULL
);
[social]
// 네이버 카카오 구글 비교후 수정 예정
CREATE TABLE IF NOT EXISTS `social` (
                                        uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                        user_name VARCHAR(255) NOT NULL,
                                        email VARCHAR(255) NOT NULL,
                                        emailyn CHAR(1) DEFAULT 'n',
                                        phone VARCHAR(255) NOT NULL,
                                        phoneyn CHAR(1) DEFAULT 'n',
                                        main_address VARCHAR(255) NOT NULL,
                                        sub_address_1 VARCHAR(255),
                                        sub_address_2 VARCHAR(255),
                                        type VARCHAR(20) NOT NULL,
                                        point INT DEFAULT 0,
                                        status VARCHAR(20) DEFAULT 'active',
                                        created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        version INT NOT NULL
);
[token]
CREATE TABLE IF NOT EXISTS `token` (
                                       uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       user_uid BIGINT,
                                       social_uid BIGINT,
                                       accessToken VARCHAR(255) NOT NULL,
                                       refreshToken VARCHAR(255) NOT NULL,
                                       version INT NOT NULL,
                                       FOREIGN KEY (user_uid) REFERENCES user(uid),
                                       FOREIGN KEY (social_uid) REFERENCES social(uid)
);
[menu]
CREATE DATABASE IF NOT EXISTS `menu`;
USE `menu`;
CREATE TABLE IF NOT EXISTS `menu` (
                                      uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                      menu_name VARCHAR(255) NOT NULL,
                                      price BIGINT NOT NULL,
                                      calorie DOUBLE NOT NULL,
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
                                      img VARCHAR(255) NOT NULL,
                                      status VARCHAR(20) DEFAULT 'active',
                                      created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      version INT NOT NULL,
                                      FOREIGN KEY (bread) REFERENCES bread(uid),
                                      FOREIGN KEY (main_material_1) REFERENCES main_material(uid),
                                      FOREIGN KEY (main_material_2) REFERENCES main_material(uid),
                                      FOREIGN KEY (main_material_3) REFERENCES main_material(uid),
                                      FOREIGN KEY (cheeze) REFERENCES cheeze(uid),
                                      FOREIGN KEY (vegetable_1) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_2) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_3) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_4) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_5) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_6) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_7) REFERENCES vegetable(uid),
                                      FOREIGN KEY (vegetable_8) REFERENCES vegetable(uid),
                                      FOREIGN KEY (sauce_1) REFERENCES sauce(uid),
                                      FOREIGN KEY (sauce_2) REFERENCES sauce(uid),
                                      FOREIGN KEY (sauce_3) REFERENCES sauce(uid)
);
[bread]
CREATE TABLE IF NOT EXISTS `bread` (
                                       uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       bread_name VARCHAR(255) NOT NULL,
                                       calorie DOUBLE NOT NULL,
                                       price INT NOT NULL,
                                       img VARCHAR(255) NOT NULL,
                                       status VARCHAR(20) DEFAULT 'active',
                                       created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       version INT NOT NULL
);
[main_material]
CREATE TABLE IF NOT EXISTS `main_material` (
                                               uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                               material_name VARCHAR(255) NOT NULL,
                                               calorie DOUBLE NOT NULL,
                                               price INT NOT NULL,
                                               img VARCHAR(255) NOT NULL,
                                               status VARCHAR(20) DEFAULT 'active',
                                               created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               version INT NOT NULL
);
[cheeze]
CREATE TABLE IF NOT EXISTS `cheeze` (
                                        uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                        cheeze_name VARCHAR(255) NOT NULL,
                                        calorie DOUBLE NOT NULL,
                                        price INT NOT NULL,
                                        img VARCHAR(255) NOT NULL,
                                        status VARCHAR(20) DEFAULT 'active',
                                        created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        version INT NOT NULL
);
[vegetable]
CREATE TABLE IF NOT EXISTS `vegetable` (
                                           uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                           vegetable_name VARCHAR(255) NOT NULL,
                                           calorie DOUBLE NOT NULL,
                                           price INT NOT NULL,
                                           img VARCHAR(255) NOT NULL,
                                           status VARCHAR(20) DEFAULT 'active',
                                           created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           version INT NOT NULL
);
[sauce]
CREATE TABLE IF NOT EXISTS `sauce` (
                                       uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       sauce_name VARCHAR(255) NOT NULL,
                                       calorie DOUBLE NOT NULL,
                                       price INT NOT NULL,
                                       img VARCHAR(255) NOT NULL,
                                       status VARCHAR(20) DEFAULT 'active',
                                       created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       version INT NOT NULL
);
[side]
CREATE TABLE IF NOT EXISTS `side` (
                                      uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                      side_name VARCHAR(255) NOT NULL,
                                      calorie DOUBLE NOT NULL,
                                      price INT NOT NULL,
                                      img VARCHAR(255) NOT NULL,
                                      status VARCHAR(20) DEFAULT 'active',
                                      created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      version INT NOT NULL
);

CREATE DATABASE IF NOT EXISTS `order`;
USE `order`;
-- order가 예약어라 orders로 변경
CREATE TABLE IF NOT EXISTS `orders` (
                                       uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                       user_uid BIGINT,
                                       social_uid BIGINT,
                                        -- 추가 여부 물어보기
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
    -- 추가 여부 물어보기
    FOREIGN KEY (menu_uid) REFERENCES menu.menu(uid)
    );


CREATE TABLE IF NOT EXISTS `custom_order` (
                                              uid BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                                            -- 추가 여부 물어보기
                                            order_uid BIGINT,
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
    FOREIGN KEY (uid) REFERENCES `orders`(uid),
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