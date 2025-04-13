$(document).ready(() => {
    $('#previewButton').click(() => {
        const customData = getCustomOrderData(); // 현재 선택된 값 가져오기

        // 값들을 문자열로 변환해서 보여주기
        const previewText = `
        빵: ${customData.bread || '-'}
        재료1: ${customData.material1 || '-'}, 재료2: ${customData.material2 || '-'}, 재료3: ${customData.material3 || '-'}
        야채1: ${customData.vegetable1 || '-'}, 야채2: ${customData.vegetable2 || '-'}, 야채3: ${customData.vegetable3 || '-'}, 야채4: ${customData.vegetable4 || '-'}
        야채5: ${customData.vegetable5 || '-'}, 야채6: ${customData.vegetable6 || '-'}, 야채7: ${customData.vegetable7 || '-'}, 야채8: ${customData.vegetable8 || '-'}
        소스1: ${customData.sauce1 || '-'}, 소스2: ${customData.sauce2 || '-'}, 소스3: ${customData.sauce3 || '-'}
        `;

        $('#previewContent').text(previewText);    // 내용 업데이트
        $('#previewBox').fadeIn();                  // 박스 보여주기
    });

    $('#addToCartButton').click(() => {
        addCustomSandwichToCart();
    });
});

// 커스텀 샌드위치 데이터 가져오기
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
        sauce3: $('#sauce3').val()
    };
}

// 장바구니에 추가하고 order 페이지로 이동
function addCustomSandwichToCart() {
    const customData = getCustomOrderData();

    $.ajax({
        url: '/carts',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            userUid: 1,
            menuName: '커스텀 샌드위치',
            price: 100,
            calorie: 500
        }),
        success: function(response) {
            const cartUid = response.uid;
            if (!cartUid) {
                console.error('cartUid가 없습니다:', response);
                alert('장바구니 추가 실패: cartUid 없음');
                return;
            }
            window.location.href = '/order'; // 바로 주문 페이지로 이동
        },
        error: function(error) {
            console.error('샌드위치 추가 실패', error.responseText);
            alert('샌드위치 추가 실패');
        }
    });
}