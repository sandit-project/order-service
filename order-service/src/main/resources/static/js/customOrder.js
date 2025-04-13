// 유저 정보 가져오기
function getUserInfo(userUid) {
    return $.ajax({
        url: `/users/${userUid}`,
        method: 'GET'
    });
}

// 주소 존재 여부 체크 API 호출
function checkUserAddress(userUid) {
    return $.ajax({
        url: `/users/${userUid}/check-address`,
        method: 'GET'
    }).then(response => {
        return response.hasAddress;
    });
}

// 스토어 리스트 가져오기
function getStores() {
    return $.ajax({
        url: '/stores',
        method: 'GET',
        contentType: 'application/json'
    }).promise();
}

// 드롭다운에 스토어 추가하기
function renderStoreDropdown() {
    getStores()
        .then(stores => {
            const $storeSelect = $('#storeSelect'); // 드롭다운 셀렉터
            $storeSelect.empty(); // 기존 옵션 삭제

            // 기본 옵션 추가
            $storeSelect.append('<option value="">스토어 선택</option>');

            stores.forEach(store => {
                const option = `<option value="${store.uid}">${store.storeName}</option>`;
                $storeSelect.append(option);
            });
        })
        .catch(error => {
            console.error('스토어 목록 불러오기 실패', error);
        });
}

let cartList = [];

$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp54787882');

    renderStoreDropdown();

    const userUid = 1; //userUid는 하드코딩
    getUserInfo(userUid)
        .then(user => {
            $('#name').val(user.userName);
            $('#phone').val(user.phone);
            $('#email').val(user.email);
            $('#address').val(user.mainAddress);
        })
        .fail(error => {
            console.error('유저 정보 불러오기 실패', error);
        });

    $('#addToCartButton').click(() => {
        addCustomSandwichToCart();
    });

    $('#customPayButton').click(async () => {
        if ($('#cartItems').children().length === 0) {
            alert('샌드위치를 먼저 담아주세요!');
            return;
        }

        const merchantUid = generateMerchantUid();
        const totalPrice = 100; // 고정 가격
        try {
            await preparePayment(merchantUid, totalPrice);
            console.log('사전 검증 완료');

            requestPayment(merchantUid, totalPrice);
        } catch (error) {
            console.error('사전 검증 실패', error);
            alert('사전 검증 실패');
        }
    });
});

function getCustomOrderData() {
    return {
        bread: $('#bread').val(),
        material1: $('#material1').val(),
        material2: $('#material2').val(),
        material3: $('#material3').val(),
        vegetable1: $('#vegetable1').val(),
        vegetable2: $('#vegetable2').val(),
        vegetable3: $('#vegetable3').val(),
        vegetable4: $('#vegetable4').val(),
        vegetable5: $('#vegetable5').val(),
        vegetable6: $('#vegetable6').val(),
        vegetable7: $('#vegetable7').val(),
        vegetable8: $('#vegetable8').val(),
        sauce1: $('#sauce1').val(),
        sauce2: $('#sauce2').val(),
        sauce3: $('#sauce3').val(),
    };

}


function addCustomSandwichToCart() {

    const customData = getCustomOrderData();
    console.log(customData);

    $.ajax({
        url: '/carts',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            userUid: 1, // 하드코딩
            menuName: '커스텀 샌드위치',
            price: 100,
            calorie: 500
        }),
        success: function(response) {
            // 서버에서 cartUid 받아옴

            const cartUid = response.uid;
            window.customCartUid = cartUid;

            if (!cartUid) {
                console.error('cartUid가 없습니다:', response);
                alert('장바구니 추가 실패: cartUid 없음');
                return;
            }

            const cartItem = {
                cartUid: cartUid,
                bread: customData.bread,
                material1: customData.material1,
                material2: customData.material2,
                material3: customData.material3,
                vegetable1: customData.vegetable1,
                vegetable2: customData.vegetable2,
                vegetable3: customData.vegetable3,
                vegetable4: customData.vegetable4,
                vegetable5: customData.vegetable5,
                vegetable6: customData.vegetable6,
                vegetable7: customData.vegetable7,
                vegetable8: customData.vegetable8,
                sauce1: customData.sauce1,
                sauce2: customData.sauce2,
                sauce3: customData.sauce3,
            };

            cartList.push(cartItem); // 메모리에 추가

            renderCartItems(); // 화면 다시 그리기
        },
        error: function(error) {
            console.error('샌드위치 추가 실패', xhr.responseText);
            alert('샌드위치 추가 실패');
        }
    });
}

function renderCartItems() {
    const $cartItems = $('#cartItems');
    $cartItems.empty();

    cartList.forEach((item, index) => {
        const itemHtml = `
            <div class="cart-item" data-index="${index}" data-cart-uid="${item.cartUid}">
                <input type="checkbox" checked disabled>
                커스텀 샌드위치 (빵:${item.bread}, 재료:${item.material1})
                <button class="edit-btn">수정</button>
                <button class="delete-btn">삭제</button>
            </div>
        `;
        $cartItems.append(itemHtml);
    });
}

$(document).on('click', '.edit-btn', function() {
    const index = $(this).closest('.cart-item').data('index');
    const item = cartList[index];

    $('#bread').val(item.bread);
    $('#material1').val(item.material1);
    $('#material2').val(item.material2);
    $('#material3').val(item.material3);
    $('#cheese').val(item.cheese);
    $('#vegetable1').val(item.vegetable1);
    $('#vegetable2').val(item.vegetable2);
    $('#vegetable3').val(item.vegetable3);
    $('#vegetable4').val(item.vegetable4);
    $('#vegetable5').val(item.vegetable5);
    $('#vegetable6').val(item.vegetable6);
    $('#vegetable7').val(item.vegetable7);
    $('#vegetable8').val(item.vegetable8);
    $('#sauce1').val(item.sauce1);
    $('#sauce2').val(item.sauce2);
    $('#sauce3').val(item.sauce3);
});

// 커스텀 샌드위치 카트 삭제
$(document).on('click', '.delete-btn', function() {
    const $cartItem = $(this).closest('.cart-item');
    const cartUid = $cartItem.data('cart-uid');
    const index = $cartItem.data('index');

    $.ajax({
        url: `/carts/${cartUid}`,
        method: 'DELETE',
        success: function() {
            cartList.splice(index, 1); // 메모리에서도 삭제
            renderCartItems(); // 다시 그리기
        },
        error: function(err) {
            console.error('샌드위치 삭제 실패', err.responseText);
            alert('삭제 실패');
        }
    });
});

//커스텀 샌드위치 카트 수정
function preparePayment(merchantUid, totalPrice) {

    const storeUid = $('#storeSelect').val();
    if (!storeUid) {
        alert('스토어를 선택해주세요!');
        throw new Error('스토어 선택 안 됨');
    }

    return $.ajax({
        url: '/orders/prepare',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            merchantUid: merchantUid,
            menuName: "커스텀 샌드위치",
            totalPrice: totalPrice,
            storeUid: storeUid,
            userUid: 1
        })
    });
}

function requestPayment(merchantUid, totalPrice) {
    const IMP = window.IMP;
    const buyer = getBuyerInfo();

    IMP.request_pay({
        pg: 'html5_inicis',
        pay_method: buyer.payMethod,
        merchant_uid: merchantUid,
        name: '커스텀 샌드위치',
        amount: totalPrice,
        buyer_name: buyer.name,
        buyer_tel: buyer.phone,
        buyer_email: buyer.email,
        buyer_addr: buyer.address
    }, function (rsp) {
        if (rsp.success) {
            $.ajax({
                url: `/orders/update-success`,
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    merchantUid: rsp.merchant_uid
                })
            }).then(() => {
                alert('결제 및 상태 업데이트 완료!');
            }).catch(error => {
                console.error('상태 업데이트 실패', error);
                alert('결제는 됐는데 서버 상태 업데이트 실패');
            });
        } else {
            // 결제 실패 -> 상태를 fail로 변경
            $.ajax({
                url: `/orders/update-fail`,
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    merchantUid: rsp.merchant_uid
                })
            }).then(() => {
                alert('결제 실패 처리 완료');
            }).catch(error => {
                console.error('결제 실패했는데 상태 업데이트 실패', error);
                alert('결제 실패 후 서버 상태 업데이트 실패');
            });
        }
    });
}

function sendCustomOrder(merchantUid) {
    const buyer = getBuyerInfo();
    const storeUid = $('#storeSelect').val();

    if (!storeUid) {
        alert('스토어를 선택해주세요!');
        throw new Error('스토어 선택 안 됨');
    }

    if (cartList.length === 0 || !cartList.every(item => item.cartUid)) {
        console.error('카트 리스트가 비어있거나 cartUid가 없습니다', cartList);
        alert('카트 정보가 유효하지 않습니다.');
        return;
    }

    const items = cartList.map(item => ({
        cartUid: item.cartUid,
        menuName: '커스텀 샌드위치',
        amount: 1,
        price: 100,
        calorie: 500
    }));

    $.ajax({
        url: '/orders/custom',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            orderRequestDTO: {
                userUid: 1,
                items: items,
                payment: buyer.payMethod,
                merchantUid: merchantUid,
                paymentSuccess: true,
                storeUid: storeUid,
                buyerName: buyer.name,
                buyerPhone: buyer.phone,
                buyerEmail: buyer.email,
                buyerAddr: buyer.address,
                price: 100
            },

            sandwiches: cartList.map(item => ({
                bread: item.bread || '',
                material1: item.material1 || '',
                material2: item.material2 || '',
                material3: item.material3 || '',
                vegetable1: item.vegetable1 || '',
                vegetable2: item.vegetable2 || '',
                vegetable3: item.vegetable3 || '',
                vegetable4: item.vegetable4 || '',
                vegetable5: item.vegetable5 || '',
                vegetable6: item.vegetable6 || '',
                vegetable7: item.vegetable7 || '',
                vegetable8: item.vegetable8 || '',
                sauce1: item.sauce1 || '',
                sauce2: item.sauce2 || '',
                sauce3: item.sauce3 || '',
            }))
        }),
        success: function () {
            alert('주문 성공!');
        },
        error: function (xhr) {
            console.error('주문 실패', xhr.responseText);
            alert('주문 실패');
        }
    });
}

function getBuyerInfo() {
    return {
        name: $('#name').val(),
        address: $('#address').val(),
        email: $('#email').val(),
        phone: $('#phone').val(),
        payMethod: $('#payMethod').val()
    };
}

//merchant Uid 생성
function generateMerchantUid() {
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const randomSixDigits = Math.floor(100000 + Math.random() * 900000);
    return `merchant_${today}_${randomSixDigits}`;
}
