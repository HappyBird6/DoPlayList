let playlistIndex = 1;
let step = 1;
let playlistList;
let interval;
let progress = 0;
let isJp = false;
let swiper;

document.addEventListener("DOMContentLoaded",function(){
    //스와이퍼
    swiper = new Swiper('.swiper', {
		// Optional parameters
		direction: 'horizontal',
		loop: false,
		// watchOverflow: true,
		// observeParents: true,
		// observeSlideChildren: true,
		observer: true,

		pagination: {
			el: '.swiper-pagination',
			type: 'bullets',
			// 'bullets', 'fraction', 'progressbar'
			clickable: true
		},
		touchStartPreventDefault: false,
		navigation: {
			nextEl: '.swiper-button-next',
			prevEl: '.swiper-button-prev',
		},

		// And if we need scrollbar
		scrollbar: {
			el: '.swiper-scrollbar',
		},
	});

	var currentIndex = swiper.activeIndex;

	const swiperMenu = document.querySelector(".swiper-scrollbar-drag");
	swiperMenu.innerHTML =
		`<div>
    		<span>STEP ${currentIndex + 1}</span>
  		</div>`;

	// 슬라이드 변경 시 이벤트 리스너 등록
	swiper.on('slideChange', function () {
		currentIndex = swiper.activeIndex;
		swiperMenu.innerHTML =
		`<div>
    		<span>STEP ${currentIndex + 1}</span>
  		</div>`;
	});

	// 슬라이드 변경 전환 완료 시 이벤트 리스너 등록
	swiper.on('slideChangeTransitionEnd', function () {
        document.querySelector(".exclamation").classList.add("disable");
        step = swiper.activeIndex+1;
	});

	document.querySelectorAll(".swiper-scrollbar-drag-background").forEach(function (e, index) {
		e.addEventListener('click', function () {
			swiper.slideTo(index);
		});
	})








    // 로고 회전 
    const circularImage = document.getElementById('logo-circle');
    let rotation = 0; // 초기 회전 각도
    function rotateImage() {
        rotation += 0.2; // 회전 각도를 1씩 증가시킵니다.
        circularImage.style.transform = `rotate(${rotation}deg)`;
        requestAnimationFrame(rotateImage); // 다음 프레임에도 회전 함수 호출
    }
    // 회전 함수 호출
    rotateImage();
    
    



    // clear - check 부분
    let intervalId;
    let fillWidth = 0;
    let fillRatio = 30;
    const btnClear = document.getElementById("btn-clear");
    const clearCheckCover = document.getElementById("clearCheckCover");
    const clearCheckFill = document.getElementById("clearCheckFill");

    btnClear.addEventListener("mouseover",function(){
        if(document.querySelector(".section-playlist .items").innerHTML.trim()==="") {
            return;
        }
        clearCheckCover.classList.remove("disable");
    });
    btnClear.addEventListener("mouseout",function(){
        clearCheckCover.classList.add("disable");
        removeClearInterval(intervalId);
        fillWidth = 0;
        fillRatio = 30;
    });
    btnClear.addEventListener('mousedown',(event)=>{        
        if(document.querySelector(".section-playlist .items").innerHTML.trim()==="") {
            return;
        }
        clearCheckCover.classList.remove("disable");
        intervalId = setInterval(function() {
            if(fillWidth >= 939){
                removeClearInterval(intervalId);
                fillWidth = 0;
                fillRatio = 30;
                clearList();
                return;
            }
            fillWidth += (939.0 / fillRatio);
            if(fillRatio < 400) fillRatio *= 1.03;6
            clearCheckFill.style.width = fillWidth+"px";

        }, 10); 
    });
    btnClear.addEventListener('mouseup',(event)=>{
        removeClearInterval(intervalId);
        fillWidth = 0;
        fillRatio = 30;
    })


    // 링크 히스토리
    const inputField = document.getElementById('link');
    const historyContainer = document.getElementById('linkHistoryContainer');

    // input 요소를 클릭할 때마다 히스토리를 표시하도록 설정
    inputField.addEventListener('click', (event) => {
        var historyItems = /*[[${history}]]*/'';
        console.log(historyItems)
        event.preventDefault();
        for(let i =0;i<historyItems.length;i++){
            let historyItem = historyItems[i];
            historyContainer.innerHTML = `
            <div class="playlist">
                <a th:href="'https://www.youtube.com/watch?v=' + ${historyItem[0]}" target="_blank">
                    <div style="height:16px; margin-right:5px"><span><i
                                class="fa-solid fa-music"></i></span></div>
                    <div th:text="${historyItem[1]}"></div>
                </a>
            </div>
        `;
        }
        
        historyContainer.style.display = 'block'; // 히스토리를 표시합니다.
    });
    document.addEventListener('click', (event) => {
        if (event.target !== inputField && event.target !== historyContainer) {
            historyContainer.style.display = 'none'; // historyContainer를 숨깁니다.
        }
      });
});





const removeClearInterval = function(interval){
    clearInterval(interval);
    clearCheckFill.style.width = 0;
    clearCheckCover.classList.add("disable");
}
const clearList = function(){
    document.querySelector(".section-playlist .items").innerHTML = "";
    playlistIndex = 1;
}