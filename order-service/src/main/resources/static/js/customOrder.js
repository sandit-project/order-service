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
        bread: parseInt($('#bread').val()) || null,
        material1: parseInt($('#material1').val()) || null,
        material2: parseInt($('#material2').val()) || null,
        material3: parseInt($('#material3').val()) || null,
        vegetable1: parseInt($('#vegetable1').val()) || null,
        vegetable2: parseInt($('#vegetable2').val()) || null,
        vegetable3: parseInt($('#vegetable3').val()) || null,
        vegetable4: parseInt($('#vegetable4').val()) || null,
        vegetable5: parseInt($('#vegetable5').val()) || null,
        vegetable6: parseInt($('#vegetable6').val()) || null,
        vegetable7: parseInt($('#vegetable7').val()) || null,
        vegetable8: parseInt($('#vegetable8').val()) || null,
        sauce1: parseInt($('#sauce1').val()) || null,
        sauce2: parseInt($('#sauce2').val()) || null,
        sauce3: parseInt($('#sauce3').val()) || null
    };
}

// 장바구니에 추가하고 order 페이지로 이동
async function addCustomSandwichToCart() {
    try {
        const customData = getCustomOrderData();

        // 1. 커스텀 주문 생성 API 호출 (옵션 데이터만 전송)
        const customResponse = await fetch("/orders/custom", {
            method: "POST",
            body: JSON.stringify(customData),
            headers: {
                "Content-Type": "application/json",
            },
        });

        let customResult = {};
        try {
            customResult = await customResponse.json();
        } catch (parseError) {
            console.error("커스텀 주문 응답 파싱 실패:", parseError);
            alert("커스텀 주문 생성 응답을 파싱하는데 실패했습니다.");
            return;
        }
        console.log("Custom order response:", customResult);
        if (!customResult.success) {
            alert(customResult.message || "커스텀 주문 생성 실패");
            return;
        }
        alert(customResult.message || "커스텀 주문 생성 완료!");

        // 2. 옵션 저장 후 장바구니 추가 API 호출
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
                console.log("Cart response:", response);
                const cartUid = response.uid;
                if (!cartUid) {
                    console.error('cartUid가 없습니다:', response);
                    alert('장바구니 추가 실패: cartUid 없음');
                    return;
                }
                alert(response.message || "장바구니 추가 성공");
                window.location.href = '/order'; // 주문 페이지로 이동
            },
            error: function(error) {
                console.error('장바구니 추가 실패', error.responseText);
                const errorMessage = (error.responseJSON && error.responseJSON.message) || '장바구니 추가 실패';
                alert(errorMessage);
            }
        });
    } catch (error) {
        console.error("addCustomSandwichToCart 에러:", error);
        alert("커스텀 주문 처리 중 에러 발생");
    }
}
