$(document).ready(() => {
    const IMP = window.IMP;
    IMP.init('imp54787882');

    $('#customPayButton').click(async () => {
        const customOrder = getCustomOrderData();
        const totalPrice = calculateCustomPrice(customOrder);
        const merchantUid = generateMerchantUid();

        try {
            await preparePayment(merchantUid, totalPrice);
            console.log('사전 검증 완료');

            requestPayment(merchantUid, totalPrice, customOrder);
        } catch (error) {
            console.error('사전 검증 실패', error);
            alert('사전 검증 실패');
        }
    });
});

// 커스텀 데이터 추출
function getCustomOrderData() {
    return {
        bread: parseInt($('#bread').val()),
        material1: parseInt($('#material1').val()),
        cheese: parseInt($('#cheese').val()),
        sauce1: parseInt($('#sauce1').val()),
        vegetable1: 1, //하드코딩
    };
}

// 커스텀 가격 계산
function calculateCustomPrice(customOrder) {
    return 6000; // 하드코딩: 샌드위치 6000원
}

// merchant_uid 생성
function generateMerchantUid() {
    const today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const randomSixDigits = Math.floor(100000 + Math.random() * 900000);
    return `merchant_${today}_${randomSixDigits}`;
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

// 결제창 띄우기
function requestPayment(merchantUid, totalPrice, customOrder) {
    const IMP = window.IMP;

    IMP.request_pay({
        pg: 'html5_inicis',
        pay_method: 'card',
        merchant_uid: merchantUid,
        name: '커스텀 샌드위치',
        amount: totalPrice,
        buyer_email: 'test@example.com',
        buyer_name: '테스트 유저',
        buyer_tel: '010-1234-5678',
        buyer_addr: '서울시 강남구'
    }, function (rsp) {
        if (rsp.success) {
            sendCustomOrder(customOrder, merchantUid);
        } else {
            alert('결제 실패: ' + rsp.error_msg);
        }
    });
}

// 커스텀 주문 저장 API 호출
function sendCustomOrder(customOrder, merchantUid) {
    const requestData = {
        ...customOrder,
        orderRequestDTO: { //주문정보
            userUid: 1, // 테스트용 user uid
            items: [{
                cartUid: 1, // 테스트용
                menuName: '커스텀 샌드위치',
                amount: 1,
                price: 1,
                calorie: 450
            }],
            payment: 'card',
            merchantUid: merchantUid,
            paymentSuccess: true
        }
    };

    $.ajax({
        url: '/orders/custom',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: (response) => {
            console.log('주문 성공', response);
            alert('커스텀 주문이 완료되었습니다!');
        },
        error: (error) => {
            console.error('주문 실패', error);
            alert('주문 실패');
        }
    });
}
