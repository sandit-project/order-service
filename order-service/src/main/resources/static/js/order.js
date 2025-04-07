// 장바구니 항목 가져오기
function getCartItems() {
    return $.ajax({
        url: '/carts',
        method: 'GET',
        contentType: 'application/json'
    });
}

// 선택된 장바구니 항목 가져오기
function getSelectedCartItems() {
    const selectedItems = [];
    $('[data-cart-item]').each(function () {
        const $checkbox = $(this).find('.cart-check');
        if ($checkbox.is(':checked')) {
            const cartUid = $(this).data('cart-uid');
            const menuName = $(this).find('.item-name').text();
            const price = parseInt($(this).find('.item-price').text()) || 0;
            const amount = parseInt($(this).find('.item-amount').text()) || 0;
            const calorie = parseInt($(this).find('.item-calorie').text()) || 0;
            selectedItems.push({ cartUid, menuName, price, calorie });
        }
    });
    return selectedItems;
}


let merchantUid = null;

$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp54787882');

    renderCartItems();
    updateTotalPrice(); // 페이지 로딩 시 금액 계산

    $('#payButton').click(async () => {
        const cartUids = getSelectedCartUids();
        const buyer = getBuyerInfo();
        const totalPrice = calculateTotal();
        const selectedItems = getSelectedCartItems();

        if (cartUids.length === 0) {
            alert('주문할 메뉴를 선택해주세요.');
            return;
        }

        let menuName = '';
        if (selectedItems.length === 1) {
            menuName = selectedItems[0].menuName;
        } else if (selectedItems.length > 1) {
            menuName = `${selectedItems[0].menuName} 외 ${selectedItems.length - 1}건`;
        }

        merchantUid = generateMerchantUid();

        try {
            await preparePayment(merchantUid, menuName, totalPrice);
            console.log('사전 검증 성공');

            requestPayment(cartUids, buyer, totalPrice, merchantUid);
        } catch (err) {
            console.error('사전 검증 실패', err);
            alert('사전 검증 실패');
        }
    });
});

// 장바구니 렌더링
async function renderCartItems() {
    const $container = $('#cartContainer');
    $container.empty(); // 이전 내용 비우기

    try {
        const items = await getCartItems(); // DB에서 가져옴
        items.forEach(item => {
            const itemHtml = `
        <div class="cart-item" data-cart-item data-cart-uid="${item.uid}">
            <input type="checkbox" class="cart-check" checked>
            <span class="item-name">${item.menuName}</span>
            <span class="item-price">${item.price}</span>원
            <span class="item-calorie">${item.calorie} kcal</span>
        </div>
      `;
            $container.append(itemHtml);
        });
    } catch (error) {
        console.error('장바구니 불러오기 실패', error);
    }
}

// 선택한 카트 항목들 cartUid 가져오기
function getSelectedCartUids() {
    const cartUids = [];
    $('[data-cart-item]').each(function () {
        if ($(this).find('.cart-check').is(':checked')) {
            const cartUid = $(this).data('cart-uid');
            if (cartUid !== undefined) {
                cartUids.push(cartUid);
            }
        }
    });
    return cartUids;
}

// 결제자 정보
function getBuyerInfo() {
    return {
        name: $('#name').val(),
        phone: $('#phone').val(),
        email: $('#email').val(),
        address: $('#address').val(),
        payMethod: $('#payMethod').val() || 'card'
    };
}

// 사전 검증 API 호출
function preparePayment(merchantUid, menuName, totalPrice) {
    return $.ajax({
        url: '/orders/prepare',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            merchantUid: merchantUid,
            menuName: menuName,
            totalPrice: totalPrice,
        })
    });
}

// 실제 결제 요청
function requestPayment(cartUids, buyer, totalPrice, merchantUid) {
    const IMP = window.IMP;
    const selectedItems = getSelectedCartItems();
    let menuName = '';

    if (selectedItems.length === 1) {
        menuName = selectedItems[0].menuName;
    } else if (selectedItems.length > 1) {
        menuName = `${selectedItems[0].menuName} 외 ${selectedItems.length - 1}건`;
    }

    console.log('선택된 아이템:', selectedItems);

    IMP.request_pay({
        pg: 'html5_inicis',
        pay_method: buyer.payMethod,
        merchant_uid: merchantUid,
        name: menuName,
        amount: totalPrice,
        buyer_name: buyer.name,
        buyer_phone: buyer.phone,
        buyer_email: buyer.email,
        buyer_addr: buyer.address
    }, function (response) {
        if (response.success) {
            sendOrderRequest(cartUids, buyer, response, true, totalPrice)
                .then(() => {
                    alert('결제 및 주문 저장 성공');
                })
                .catch(() => {
                    alert('주문 저장 실패');
                });
        } else {
            sendOrderRequest(cartUids, buyer, response, false, totalPrice)
                .then(() => {
                    alert('결제는 실패했지만 주문 저장 성공');
                })
                .catch(() => {
                    alert('결제 실패 + 주문 저장 실패');
                });
        }
    });
}

// 결제 후 서버에 주문 전송
function sendOrderRequest(cartUids, buyer, paymentResponse, paymentSuccess, totalPrice) {
    return new Promise((resolve, reject) => {
        const selectedItems = getSelectedCartItems();

        const items = selectedItems.map(item => ({
            cartUid: item.cartUid,  // 장바구니 ID
            menuName: item.menuName,
            price: item.price,
            calorie: item.calorie
        }));


        $.ajax({
            type: 'POST',
            url: '/orders',
            contentType: 'application/json',
            data: JSON.stringify({
                userUid: 1,
                items: items,
                payment: buyer.payMethod,
                merchantUid: paymentResponse.merchant_uid,
                paymentSuccess: paymentSuccess,
                buyerName: buyer.name,
                buyerPhone: buyer.phone,
                buyerEmail: buyer.email,
                buyerAddr: buyer.address,
                price: totalPrice
            }),
            success: function(response) {
                console.log('주문 저장 성공', response);
                resolve(response);
            },
            error: function(xhr, status, error) {
                console.error('주문 저장 실패', xhr.responseText);
                reject(error);
            }
        });
    });
}

// 총 금액 계산
function calculateTotal() {
    let total = 0;
    $('[data-cart-item]').each(function () {
        if ($(this).find('.cart-check').is(':checked')) {
            const price = parseInt($(this).find('.item-price').text()) || 0;
            total += price;
        }
    });
    return total;
}

// 총 금액 표시 업데이트
function updateTotalPrice() {
    const totalPrice = calculateTotal();
    $('#totalPrice').text(`${totalPrice.toLocaleString('ko-KR')} 원`);
}

// merchant_uid 생성
function generateMerchantUid() {
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const randomSixDigits = Math.floor(100000 + Math.random() * 900000);
    return `merchant_${today}_${randomSixDigits}`;
}

// 장바구니 비우기
function clearCart() {
    $('[data-cart-item]').remove();
    updateTotalPrice();
    alert('장바구니를 비웠습니다.');
}

// 체크박스 변경시 금액 업데이트
$(document).on('change', '.cart-check', function () {
    updateTotalPrice();
});