let cart = [];

$(document).ready(function () {
    $('#addToCartButton').click(function () {
        const customOrder = {
            bread: $('#bread').val(),
            material1: $('#material1').val(),
            material2: $('#material2').val(),
            material3: $('#material3').val(),
            cheese: $('#cheese').val(),
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
            sauce3: $('#sauce3').val()
        };
        cart.push(customOrder);
        console.log("카트에 담은 커스텀 샌드위치:", customOrder);
        alert("커스텀 샌드위치가 카트에 담겼습니다!");
    });

    $('#payButton').click(function () {
        if (cart.length === 0) {
            alert('카트가 비어있습니다.');
            return;
        }

        // 결제창 띄우는 로직 (IMP.request_pay) 추가할 수 있음

        // 백엔드로 커스텀 주문 전송
        $.ajax({
            url: '/orders/custom', // 여기에 매핑해야함
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(cart[0]), // 임시로 첫 번째 아이템만 전송
            success: function (response) {
                console.log("서버 응답:", response);
                alert('주문이 완료되었습니다.');
                cart = [];
            },
            error: function (xhr) {
                console.error("에러:", xhr.responseText);
                alert('주문에 실패했습니다.');
            }
        });
    });
});
