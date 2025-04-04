let merchantUid = null;

$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp54787882');

    updateTotalPrice(); // 페이지 로딩시 가격 갱신

    $('#payButton').click(async () => {
        const items = getSelectedItems();
        const buyer = getBuyerInfo();
        const totalPrice = calculateTotal(items);

        if (items.length === 0) {
            alert('주문할 메뉴를 선택해주세요.');
            return;
        }

        // 1. merchant_uid 생성
        const newMerchantUid = generateMerchantUid();
        console.log("생성된 merchant uid:", newMerchantUid);

        // 2. 사전 검증 요청
        try {
            const response = await preparePayment(newMerchantUid, totalPrice);
            console.log('사전 검증 완료');

            // 3. 결제창 띄우기
            requestPayment(items, buyer, totalPrice, newMerchantUid);
        } catch (err) {
            console.error('사전 검증 실패', err);
            alert('사전 검증 실패');
        }
    });
});

// 사전 검증 호출
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

    console.log("결제 요청 직전", merchantUid, totalPrice);
}



// 결제 요청
function requestPayment(items, buyer, totalPrice, merchantUid) {
    const IMP = window.IMP;
    const itemName = formatItemName(items);
    console.log("IMP 호출 직전 merchantUid:", merchantUid);

    IMP.request_pay({
        pg: 'html5_inicis',
        pay_method: buyer.payMethod,
        merchant_uid: merchantUid,
        name: itemName,
        amount: totalPrice,
        buyer_name: buyer.name,
        buyer_phone: buyer.phone,
        buyer_addr: buyer.address,
        buyer_email: buyer.email
    }, function (response) {
        if (response.success) {
            sendOrderRequest(items, buyer, response, true);
            console.log('결제 응답::' , response);
        } else {
            sendOrderRequest(items, buyer, response, false);
            alert('결제 실패: ' + response.error_msg);
        }
    });
}

// 주문 전송
function sendOrderRequest(items, buyer, paymentResponse, paymentSuccess) {
    const orderData = {
        userUid: 1, // 일단 하드코딩
        items: items,
        payment: buyer.payMethod,
        merchantUid: paymentResponse.merchant_uid,
        paymentSuccess: paymentSuccess,
        buyerEmail: buyer.email,
        buyerName: buyer.name,
        buyerPhone: buyer.phone,
        buyerAddr: buyer.address
    };

    console.log("주문 정보:", orderData);

    $.ajax({
        url: '/orders',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderData),
        success: (response) => {
            console.log(response);
            alert(paymentSuccess ? '주문 및 결제 성공' : '결제 실패 - 주문만 저장됨');
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

// 장바구니 비우기
function clearCart() {
    $('[data-menu-item]').remove();
    updateTotalPrice();
    alert('장바구니를 비웠습니다.');
}

// 상품명 포맷
function formatItemName(items) {
    if (items.length === 1) return items[0].menuName;
    return `${items[0].menuName} 외 ${items.length - 1}건`;
}

// 총 금액 계산
function calculateTotal(items) {
    if (!items || items.length === 0) return 0;
    return items.reduce((sum, item) => sum + (item.price * item.amount), 0);
}

// 총 금액 업데이트
function updateTotalPrice() {
    const items = getSelectedItems();
    const totalPrice = calculateTotal(items);
    $('#totalPrice').text(`${totalPrice.toLocaleString('ko-KR')} 원`);
}

// 장바구니 선택된 항목 가져오기
function getSelectedItems() {
    const items = [];

    $('[data-menu-item]').each(function () {
        if (!$(this).find('.menu-check').is(':checked')) return;

        const menuName = $(this).find('[name$=".menuName"]').val();
        const amount = parseInt($(this).find('[name$=".amount"]').val()) || 1;
        const price = parseInt($(this).find('[name$=".price"]').val()) || 0;
        const calorie = parseFloat($(this).find('[name$=".calorie"]').val()) || 0.0;

        if (menuName && amount > 0 && price >= 0 && calorie >= 0) {
            items.push({ menuName, amount, price, calorie });
        }
    });

    return items;
}

// 주문자 정보 가져오기
function getBuyerInfo() {
    return {
        name: $('#name').val(),
        phone: $('#phone').val(),
        email: $('#email').val(),
        address: $('#address').val(),
        payMethod: $('#payMethod').val() || 'card'
    };
}

// merchant_uid 생성
function generateMerchantUid() {
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const randomSixDigits = Math.floor(100000 + Math.random() * 900000);
    return `merchant_${today}_${randomSixDigits}`;
}

// 이벤트 바인딩 (수량 변경시 총 금액 갱신)
$(document).on('change', '.menu-check, input[name$=".amount"]', function () {
    updateTotalPrice();
});
