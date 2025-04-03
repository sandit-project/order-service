$(document).ready(() => {
    const menuUid = $('#menuUid').val(); // 단일 주문용 hidden input
    const IMP = window.IMP;
    IMP.init('imp54787882');

    if (menuUid) {
        fetchMenuFromDB(menuUid);
    }


    //총 금액 실시간으로 변동
    $(document).on('input change', 'input[name$=".amount"], .menu-check', updateTotalPrice);
    updateTotalPrice(); // 초기 로딩 시 계산

    $('#payButton').click(() => {
        const merchantUid = 'merchant_' + new Date().getTime();
        const buyerInfo = {
            name: $('#name').val(),
            tel: $('#tel').val(),
            address: $('#address').val(),
            payMethod: $('#payMethod').val() || 'card'
        };

        const selectedItems = getSelectedOrderItems();
        if (selectedItems.length === 0) {
            alert("선택된 메뉴가 없습니다.");
            return;
        }

        const totalPrice = selectedItems.reduce((sum, item) => sum + (item.price * item.amount), 0);
        $('#totalPrice').text(`${totalPrice}원`);
        const itemName = getOrderItemNames(selectedItems);

        console.log("결제 요청:", { buyerInfo, selectedItems, totalPrice, itemName });

        IMP.request_pay({
            pg: 'html5_inicis',
            pay_method: buyerInfo.payMethod,
            merchant_uid: merchantUid,
            name: itemName,
            amount: totalPrice,
            buyer_name: buyerInfo.name,
            buyer_tel: buyerInfo.tel,
            buyer_addr: buyerInfo.address
        }, (rsp) => paymentCallback(rsp, selectedItems, buyerInfo));
    });
});

function updateTotalPrice() {
    const selectedItems = getSelectedOrderItems();
    const totalPrice = selectedItems.reduce((sum, item) => sum + (item.price * item.amount), 0);
    $('#totalPrice').text(`${totalPrice.toLocaleString()}원`);
}

//선택된 메뉴명 파싱
function getSelectedOrderItems() {
    const items = [];

    $('[data-menu-item]').each(function () {
        const checked = $(this).find('.menu-check').is(':checked');
        if (!checked) return;

        const menuName = $(this).find('[name$=".menuName"]').val();
        const amount = parseInt($(this).find('[name$=".amount"]').val()) || 0;
        const price = parseInt($(this).find('[name$=".price"]').val()) || 0;

        if (amount > 0 && menuName) {
            items.push({ menuName, amount, price });
        }
    });

    return items;
}

//결제창에 뜨는 상품 명
function getOrderItemNames(items) {
    if (!items || items.length === 0) return '상품 없음';
    if (items.length === 1) return items[0].menuName;
    return `${items[0].menuName} 외 ${items.length - 1}건`;
}

// 결제 성공 후 서버로 주문 전송
function paymentCallback(rsp, items, buyerInfo) {
    if (!rsp.success) {
        alert('결제 실패: ' + rsp.error_msg);
        console.error('결제 실패 응답:', rsp);
        return;
    }

    const orderData = {
        items: items,
        buyer: {
            name: buyerInfo.name,
            address: buyerInfo.address,
            tel: buyerInfo.tel,
            payMethod: buyerInfo.payMethod,
            merchantUid: rsp.merchant_uid,
            impUid: rsp.imp_uid
        }
    };

    console.log("서버 전송용 주문 데이터:", orderData);

    $.ajax({
        url: '/orders',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderData),
        success: (res) => {
            alert('결제 및 주문 저장 성공!');
            console.log('주문 저장 응답:', res);
        },
        error: (xhr, status, error) => {
            alert('주문 저장 실패');
            console.error('서버 오류:', xhr.responseText);
        }
    });
}

//단일 주문일 경우 메뉴 정보 서버에서 가져옴
function fetchMenuFromDB(menuUid) {
    $.ajax({
        url: '/menu/' + menuUid,
        method: 'GET',
        dataType: 'json',
        success: (menuData) => {
            console.log('가져온 메뉴 정보: ', menuData);

            $('input[name$=".menuName"]').val(menuData.menuName);
            $('input[name$=".price"]').val(menuData.price);
        },
        error: (err) => {
            console.error('메뉴 정보를 불러오지 못했습니다.', err);
            alert('메뉴 정보 조회 실패');
            $('#payButton').prop('disabled', true);
        }
    });
}
