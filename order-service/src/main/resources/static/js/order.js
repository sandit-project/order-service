$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp82833256');

    let merchantUid = null;

    // 페이지 로드 시 사전 검증용 merchantUid 생성
    generateMerchantUid();

    $('#payButton').click(async () => {
        const items = getSelectedItems();
        if (items.length === 0) {
            alert('주문할 메뉴를 선택해주세요.');
            return;
        }

        const buyer = getBuyerInfo();
        const totalPrice = calculateTotal(items);
        const itemName = formatItemName(items);

        // 결제 요청
        IMP.request_pay({
            pg: 'html5_inicis',
            pay_method: buyer.payMethod,
            merchant_uid: merchantUid,
            name: itemName,
            amount: totalPrice,
            buyer_name: buyer.name,
            buyer_tel: buyer.tel,
            buyer_addr: buyer.address
        }, response => {
            if (response.success) {
                sendOrderRequest(items, buyer, response, true);
            } else {
                sendOrderRequest(items, buyer, response, false);
                alert('결제 실패: ' + response.error_msg);
            }
        });
    });
});

// 사전 검증용 merchantUid 생성
function generateMerchantUid() {
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    merchantUid = `merchant_${today}_${Math.floor(Math.random() * 1000000)}`;
}

// 장바구니 아이템 정보 추출
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

// 결제자 정보 추출
function getBuyerInfo() {
    return {
        name: $('#name').val(),
        tel: $('#tel').val(),
        email: $('#email').val(),
        address: $('#address').val(),
        payMethod: $('#payMethod').val() || 'card'
    };
}
// 총 금액 계산 함수
function calculateTotal(items) {
    if (!items || items.length === 0) return 0;
    return items.reduce((sum, item) => sum + (item.price * item.amount), 0);
}

// 실시간 총 금액 업데이트 함수
function updateTotalPrice() {
    const items = getSelectedItems();
    const totalPrice = calculateTotal(items);
    $('#totalPrice').text(`${totalPrice.toLocaleString('ko-KR')} 원`);
}

// 이벤트 바인딩
$(document).on('change', '.menu-check, input[name$=".amount"]', function () {
    updateTotalPrice();
});

// 페이지 로딩시 초기화
$(document).ready(() => {
    updateTotalPrice();
});

// 결제창 상품명
function formatItemName(items) {
    if (items.length === 1) return items[0].menuName;
    return `${items[0].menuName} 외 ${items.length - 1}건`;
}

// 장바구니 비우는 함수 (주문 성공 시)
function clearCart() {
    $('[data-menu-item]').each(function () {
        $(this).remove();
    });
    alert('장바구니를 비웠습니다.');
}

// 서버로 주문 전송
function sendOrderRequest(items, buyer, paymentResponse, paymentSuccess) {
    const orderData = {
        // userUid는 백엔드에서 세션이나 토큰으로 받는 경우 제외 가능
        userUid: 1,
        items: items,
        payment: buyer.payMethod,
        merchantUid: paymentResponse.merchant_uid,
        paymentSuccess: paymentSuccess,
        buyerEmail: buyer.email,
        buyerName: buyer.name,
        buyerTel: buyer.tel,
        buyerAddr: buyer.address
    };

    console.log("주문 정보::", orderData);

    $.ajax({
        url: '/orders',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderData),
        success: (response) => {
            console.log(response);
            alert(paymentSuccess ? '주문 및 결제 성공' : '결제 실패 - 주문만 저장됨');
            if (paymentSuccess) {
                // 성공했으면 장바구니 비우기
                clearCart();
            }
        },
        error: (err) => {
            alert('주문 저장 실패');
            console.error(err.responseText);
        }
    });
}