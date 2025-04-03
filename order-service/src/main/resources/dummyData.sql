-- 1. Bread 더미 데이터
INSERT INTO bread (bread_name, calorie, price, img, status, version)
VALUES ('화이트빵', 200, 500, 'white_bread.png', 'active', 1);

-- 2. Material 더미 데이터 (예시로 3개)
INSERT INTO material (material_name, calorie, price, img, status, version)
VALUES
    ('닭고기', 150, 1000, 'chicken.png', 'active', 1),
    ('소고기', 200, 1500, 'beef.png', 'active', 1),
    ('돼지고기', 250, 1200, 'pork.png', 'active', 1);

-- 3. Cheese 더미 데이터
INSERT INTO cheese (cheese_name, calorie, price, img, status, version)
VALUES ('모짜렐라', 100, 800, 'mozzarella.png', 'active', 1);

-- 4. Vegetable 더미 데이터 (필수 1번은 NOT NULL, 나머지는 선택)
INSERT INTO vegetable (vegetable_name, calorie, price, img, status, version)
VALUES
    ('상추', 10, 300, 'lettuce.png', 'active', 1),
    ('토마토', 15, 400, 'tomato.png', 'active', 1),
    ('양파', 20, 200, 'onion.png', 'active', 1),
    ('오이', 5, 250, 'cucumber.png', 'active', 1),
    ('피망', 30, 500, 'bellpepper.png', 'active', 1),
    ('당근', 25, 350, 'carrot.png', 'active', 1),
    ('양배추', 18, 300, 'cabbage.png', 'active', 1),
    ('무', 12, 220, 'radish.png', 'active', 1);

-- 5. Sauce 더미 데이터 (sauce1은 NOT NULL)
INSERT INTO sauce (sauce_name, calorie, price, img, status, version)
VALUES
    ('마요네즈', 90, 100, 'mayo.png', 'active', 1),
    ('케첩', 50, 80, 'ketchup.png', 'active', 1),
    ('머스타드', 40, 120, 'mustard.png', 'active', 1);

-- 6. Side 더미 데이터 (선택 사항)
INSERT INTO side (side_name, calorie, price, img, status, version)
VALUES ('감자튀김', 300, 1500, 'fries.png', 'active', 1);

---------------------------------------------------------
-- 7. Menu 더미 데이터
-- 여기서는 주문 시 저장할 때 메뉴 테이블에는 menu_name(메뉴 이름)과 가격, 칼로리 등
-- 그리고 외래키로 참조하는 각 재료의 uid를 서브쿼리로 가져오는 방식으로 처리합니다.
---------------------------------------------------------
INSERT INTO menu (
    menu_name, price, calorie,
    bread, material1, material2, material3, cheese,
    vegetable1, vegetable2, vegetable3, vegetable4, vegetable5, vegetable6, vegetable7, vegetable8,
    sauce1, sauce2, sauce3, img, version
)
VALUES (
           '치킨버거',          -- 메뉴 이름
           8000,                -- 가격
           750,                 -- 칼로리
           (SELECT uid FROM bread WHERE bread_name = '화이트빵' LIMIT 1),      -- bread
       (SELECT uid FROM material WHERE material_name = '닭고기' LIMIT 1),    -- material1
       (SELECT uid FROM material WHERE material_name = '소고기' LIMIT 1),    -- material2 (예시)
    NULL,                                                            -- material3 (선택)
       (SELECT uid FROM cheese WHERE cheese_name = '모짜렐라' LIMIT 1),      -- cheese
       (SELECT uid FROM vegetable WHERE vegetable_name = '상추' LIMIT 1),      -- vegetable1 (필수)
       (SELECT uid FROM vegetable WHERE vegetable_name = '토마토' LIMIT 1),     -- vegetable2
       (SELECT uid FROM vegetable WHERE vegetable_name = '양파' LIMIT 1),       -- vegetable3
       (SELECT uid FROM vegetable WHERE vegetable_name = '오이' LIMIT 1),       -- vegetable4
       (SELECT uid FROM vegetable WHERE vegetable_name = '피망' LIMIT 1),       -- vegetable5
       (SELECT uid FROM vegetable WHERE vegetable_name = '당근' LIMIT 1),       -- vegetable6
       (SELECT uid FROM vegetable WHERE vegetable_name = '양배추' LIMIT 1),     -- vegetable7
       (SELECT uid FROM vegetable WHERE vegetable_name = '무' LIMIT 1),         -- vegetable8
       (SELECT uid FROM sauce WHERE sauce_name = '마요네즈' LIMIT 1),          -- sauce1 (필수)
       (SELECT uid FROM sauce WHERE sauce_name = '케첩' LIMIT 1),             -- sauce2
    NULL,                                                            -- sauce3 (선택)
    'burger.png',                                                  -- 이미지 파일명
    1                                                              -- version
    );

INSERT INTO user (
    user_id, password, user_name, email, emailyn, phone, phoneyn,
    main_address, sub_address_1, sub_address_2,
    point, status, created_date, version
) VALUES (
             'test',
             'test',
             'test',
             'dummy@example.com',
             'Y',
             '010-1234-5678',
             'Y',
             '서울시 강남구 테헤란로 123',
             '위워크 빌딩 3층',
             '개발자 자리 옆',
             100,
             'active',
             CURRENT_TIMESTAMP,
             1
         );

