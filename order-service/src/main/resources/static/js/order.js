let merchantUid = null;

$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp54787882');

    updateTotalPrice(); // 페이지 로딩 시 금액 계산

    $('#payButton').click(async () => {
        const cartUids = getSelectedCartUids();
        const buyer = getBuyerInfo();
        const totalPrice = calculateTotal();

        if (cartUids.length === 0) {
            alert('주문할 메뉴를 선택해주세요.');
            return;
        }

        merchantUid = generateMerchantUid(); // 1. merchant_uid 새로 생성

        try {
            await preparePayment(merchantUid, totalPrice); // 2. 사전 검증
            console.log('사전 검증 성공');

            requestPayment(cartUids, buyer, totalPrice); // 3. 결제창 띄우기
        } catch (err) {
            console.error('사전 검증 실패', err);
            alert('사전 검증 실패');
        }
    });
});

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
function preparePayment(merchantUid, totalPrice) {
    return $.ajax({
        url: '/orders/prepare',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            merchantUid: merchantUid,
            totalPrice: totalPrice
        })
    });
}

// 실제 결제 요청
function requestPayment(cartUids, buyer, totalPrice) {
    const IMP = window.IMP;
    IMP.request_pay({
        pg: 'html5_inicis',
        pay_method: buyer.payMethod,
        merchant_uid: merchantUid,
        name: '선택한 메뉴 결제',
        amount: totalPrice,
        buyer_name: buyer.name,
        buyer_phone: buyer.phone,
        buyer_email: buyer.email,
        buyer_addr: buyer.address
    }, function (rsp) {
        if (rsp.success) {
            sendOrderRequest(cartUids, buyer, rsp, true);
        } else {
            sendOrderRequest(cartUids, buyer, rsp, false);
            alert('결제 실패: ' + rsp.error_msg);
        }
    });
}

// 결제 후 서버에 주문 전송
function sendOrderRequest(cartUids, buyer, paymentResponse, paymentSuccess) {
    const orderData = {
        userUid: 1, // 하드코딩
        cartUids: cartUids,
        payment: buyer.payMethod,
        merchantUid: paymentResponse.merchant_uid,
        paymentSuccess: paymentSuccess,
        buyerName: buyer.name,
        buyerPhone: buyer.phone,
        buyerEmail: buyer.email,
        buyerAddr: buyer.address
    };

    console.log('보낼 주문 데이터:', orderData);

    $.ajax({
        url: '/orders',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderData),
        success: (response) => {
            console.log('주문 성공:', response);
            alert(paymentSuccess ? '주문 및 결제 성공' : '주문만 저장됨');
            if (paymentSuccess) {
                clearCart();
            }
        },
        error: (err) => {
            alert('주문 저장 실패');
            console.error(err.responseText);
        }
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