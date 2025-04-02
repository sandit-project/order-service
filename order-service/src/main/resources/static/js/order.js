$(document).ready(() => {
    const menuUid = $('#menuUid').val();

    // iamport 초기화
    const IMP = window.IMP;
    IMP.init('imp54787882');

    if (menuUid) {
        fetchMenuFromDB(menuUid);
    }

    $('#payButton').click(() => {
        const menu = $('#payButton').data('menu');

        if (!menu) {
            alert("메뉴 정보를 불러오는 중입니다.");
            return;
        }

        const merchantUid = 'merchant_' + new Date().getTime();

        IMP.request_pay({
            pg: 'html5_inicis',
            pay_method: 'card',
            merchant_uid: merchantUid,
            name: menu.menuName,
            amount: menu.price,
            buyer_email: 'buyer@example.com',
            buyer_name: '홍길동',
            buyer_tel: '010-1234-5678',
            buyer_addr: '서울특별시 강남구',
            buyer_postcode: '123-456'
        }, function (rsp) {
            if (rsp.success) {
                alert('결제 성공!');
                console.log('결제 성공 응답:', rsp);
            } else {
                alert('결제 실패: ' + rsp.error_msg);
                console.log('결제 실패 응답:', rsp);
            }
        });
    });
});

// DB에서 메뉴 정보를 가져오는 함수
function fetchMenuFromDB(menuUid) {
    $.ajax({
        url: '/menu/' + menuUid,
        method: 'GET',
        dataType: 'json',
        success: (menuData) => {
            console.log('가져온 메뉴 정보: ', menuData);

            $('#menuName').text(menuData.menuName);
            $('#menuPrice').text(Number(menuData.price).toLocaleString() + '원');

            $('#payButton').data('menu', menuData);
        },
        error: (err) => {
            console.error('메뉴 정보를 불러오지 못했습니다.', err);
            alert('메뉴 정보 조회 실패')
            $('#payButton').prop('disabled', true);
        }
    });
}
