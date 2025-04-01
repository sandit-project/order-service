$(document).ready(function(){
    var IMP = window.IMP;
    IMP.init('imp54787882');

    // 결제 요청 함수
    $('#payButton').click(function(){
        // 결제 고유 식별자 생성 (예시로 타임스탬프 사용)
        var merchantUid = 'merchant_' + new Date().getTime();

        // 결제 요청 호출
        IMP.request_pay({
            pg : 'html5_inicis',      // PG사 설정 (필요에 따라 변경)
            pay_method : 'card',       // 결제 수단 설정 (카드)
            merchant_uid : merchantUid, // 고유 주문번호
            name : '테스트 상품',      // 상품명
            amount : 100,              // 결제 금액
            buyer_email : 'buyer@example.com',
            buyer_name : '홍길동',
            buyer_tel : '010-1234-5678',
            buyer_addr : '서울특별시 강남구',
            buyer_postcode : '123-456'
        }, function(rsp){
            if (rsp.success) {
                // 결제 성공 시 로직
                alert('결제 성공');
                console.log('결제 성공 응답:', rsp);
            } else {
                // 결제 실패 시 로직
                alert('결제 실패: ' + rsp.error_msg);
                console.log('결제 실패 응답:', rsp);
            }
        });
    });
});
