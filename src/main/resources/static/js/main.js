const submitLink = async function () {
    let loading = document.querySelector(".section-playlist-show .slide-header .loading");
    const input = document.getElementById("link");
    let value = input.value;
    if (!value){
        alert("예시 링크를 제출합니다.");
        value = "https://www.youtube.com/watch?v=ImqDaISiOac";
    };
    try {
        loading.classList.remove("disable");

        const response = await fetch(`/getPlayList`, {
            method: 'POST',
            body: value
        });
        if (!response.ok) {
            throw new Error('서버 응답이 실패했습니다.');
        }
        const flag = await response.json();
        // if(flag[0]==="-1"){
        //     alert("로그인 이후 이용 가능합니다.");
        //     return;
        // }
        const items = document.querySelector(".section-playlist-show").querySelector(".items");
        let channel = flag[1];
        let title = flag[2];
        items.innerHTML +=`
            <div class='channel-info'>
                <div class='channel-name'>
                    <span>[${channel}]</span>
                </div>
                <div class='channel-title'>
                    <span>${title}</span>
                </div>
            </div>
        `
        var itemList = JSON.parse(flag[3]);
        for (let i = 0;i<itemList.length;i++) {
            items.innerHTML +=
                `<div class='item' data-check='false' id='music-${playlistIndex}'>
                    <div class='title-section'>
                        <div class='index'><span>${playlistIndex}</span></div>
                        <div class='title'>
                            <a href="https://www.youtube.com/results?search_query=${itemList[i]}&sp=CAM%253D" target="_blank">
                                ${itemList[i]}
                            </a>
                        </div>
                    </div>
                    <div class='user-section'>
                        <span class='add' onclick='addMusic(${playlistIndex})'><i class="fa-regular fa-square-plus"></i></span>
                    </div>
                    <div class='user-section disable'>
                        <span onclick='removeMusic(${playlistIndex})'><i class="fa-regular fa-square-minus"></i></span>
                    </div>
                    <div class='user-section disable' style='color:rgb(255, 90, 90);'>
                        <span style='cursor:auto !important'><i class="fa-regular fa-square-check"></i></span>
                    </div>
                </div>`;
            playlistIndex += 1;
        }
        let history = document.querySelector("#history .content");
        if(history){
            let h = history.querySelectorAll(".playlist a");
            let check = true;
            h.forEach(function(e){
                if(value.includes(e.href)){
                    check = false;
                }
            })
            if(check){
                history.innerHTML +=`
                    <div class="playlist">
                        <a href="${value}" target="_blank">
                            <div style="height:16px; margin-right:5px"><span><i class="fa-solid fa-music"></i></span></div>
                            <div>${title}</div>
                        </a>
                    </div> 
                `
            } 
        }
        
    } catch (error) {
        console.error('DB 호출 중 오류가 발생했습니다.', error);
    } finally {
        let loading = document.querySelector(".section-playlist-show .slide-header .loading");
        loading.classList.add("disable");

    }
}
const addEventToAddBtn = function () {
    const btns = document.querySelectorAll(".item .user-section .add");
    for (let i = 0; i < btns.length; i++) {
        btns[i].addEventListener("mouseover", e => {
            btns[i].parentNode.parentNode.style.color = "rgba(0,0,0,.5)";
            btns[i].parentNode.parentNode.querySelector(".title a").style.color = "rgba(0,0,0,.5)";
        })
        btns[i].addEventListener("mouseout", e => {
            btns[i].parentNode.parentNode.style.color = "black";
            btns[i].parentNode.parentNode.querySelector(".title a").style.color = "black";
            btns[i].parentNode.parentNode.querySelector(".title a").addEventListener("mouseover", e => {
                btns[i].parentNode.parentNode.querySelector(".title a").style.color = "blueviolet";
            })
            btns[i].parentNode.parentNode.querySelector(".title a").addEventListener("mouseout", e => {
                btns[i].parentNode.parentNode.querySelector(".title a").style.color = "black";
            })
        })
    }
}
const addEventToRemoveBtn = function () {
    const btns = document.querySelectorAll(".item .user-section .add");
    for (let i = 0; i < btns.length; i++) {
        btns[i].addEventListener("mouseover", e => {
            btns[i].parentNode.parentNode.style.color = "rgba(0,0,0,.5)";
            btns[i].parentNode.parentNode.querySelector(".title a").style.color = "rgba(0,0,0,.5)";
        })
        btns[i].addEventListener("mouseout", e => {
            btns[i].parentNode.parentNode.style.color = "black";
            btns[i].parentNode.parentNode.querySelector(".title a").style.color = "black";
            btns[i].parentNode.parentNode.querySelector(".title a").addEventListener("mouseover", e => {
                btns[i].parentNode.parentNode.querySelector(".title a").style.color = "blueviolet";
            })
            btns[i].parentNode.parentNode.querySelector(".title a").addEventListener("mouseout", e => {
                btns[i].parentNode.parentNode.querySelector(".title a").style.color = "black";
            })
        })
    }
}
const login = async function () {
    window.location.href = "https://accounts.google.com/o/oauth2/auth?access_type=offline&approval_prompt=force&client_id=48707345453-b5i5kn49fgqoievl0koq889r11rg3gom.apps.googleusercontent.com&redirect_uri=http://localhost:8080/login/oauth2/code/google&response_type=code&scope=https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.force-ssl";
}

const openUserMenu = function (element) {
    let playlist = document.getElementById("playlist");
    let history = document.getElementById("history");
    if(element.dataset.type==='p' && !playlist.classList.contains("disable")) return;
    if(element.dataset.type==='h' && !history.classList.contains("disable")) return;
    playlist.classList.toggle("disable");
    history.classList.toggle("disable");
    let icons = document.querySelectorAll(".user-menu .icon");
    icons[0].classList.toggle("check");
    icons[1].classList.toggle("check");
}
const addMusic = async function (index) {

    const check = document.querySelector(".section-playlist-show .slide-header .japanese-check span").dataset.check;
    let type = 0;
    if(check==='Y') type = 1;
    else if(check==='N') type = 0;

    const music = document.getElementById("music-" + index);
    if (music.dataset.check === 'true') return;
    music.dataset.check = 'true';

    const titleTag = music.querySelector('.title a');

    const icons = music.querySelectorAll('.user-section');
    icons[0].querySelector('i').classList.add("fa-shake");
    setTimeout(function () {
        icons[0].querySelector('i').classList.remove("fa-shake");
        icons[0].classList.add("disable");
        icons[1].classList.remove("disable");
        icons[1].style.color = "rgba(255,90,90)";
        titleTag.addEventListener("mouseover", function () {
            titleTag.style.color = "blueviolet"
        });
        titleTag.addEventListener("mouseout", function () {
            titleTag.style.color = "black"
        });

        music.querySelector('.index').style.color = "rgba(255,90,90,1)";
    }, 500);

    let suffix = type === 0? 'Auto-generated':'Official';

    const title = titleTag.innerHTML;
    const link = titleTag.href;
    let modifiedLink = link.split("&")[0]+"+'"+suffix+"'&"+link.split("&")[1];

    const sectionSelected = document.querySelector(".section-selected").querySelector(".items");
    sectionSelected.innerHTML +=
        `<div class='item' id='s_music-${index}'>
            <div class='title-section'>
                <div class='index'><span>${index}</span></div>
                <div class='thumbnail-section'>
                    <div class="disable loading">
                        <div class="rect1"></div>
                        <div class="rect2"></div>
                        <div class="rect3"></div>
                        <div class="rect4"></div>
                        <div class="rect5"></div>
                    </div>
                    <div><img class='thumbnail'></img></div>
                </div>
                <div class='title-container'>
                    <div class='title'>
                        <a href="${modifiedLink}" target="_blank">
                            ${title}
                        </a>
                    </div>
                    <div class='yt-title'></div>
                </div>
            </div>
            <div class='user-section'>
                <div class='playlist'>
                    ${getMyPlaylist()}
                </div>
                <span onclick='removeMusic(${index})'>
                    <i class="fa-regular fa-square-minus"></i>
                </span>
            </div>
        </div>`;
    const item = document.getElementById(`s_music-${index}`);
    let loading = item.querySelector(".thumbnail-section .loading");
    loading.classList.remove("disable");



    try {
        enqueueAsyncTask(index,title,type);
    } catch (error) {
        console.error("addMusic 에러", error);
    }
}
const removeMusic = function (index) {
    const music = document.getElementById("music-" + index);
    const s_music = document.getElementById("s_music-" + index);
    const titleTag = music.querySelector(".title a");

    // 원상복귀
    music.dataset.check = 'false';

    titleTag.style.color = "black";
    titleTag.addEventListener("mouseout", function () {
        titleTag.style.color = "black"
    });

    const icons = music.querySelectorAll('.user-section');
    icons[1].querySelector('i').classList.add("fa-shake");
    setTimeout(function () {
        icons[1].querySelector('i').classList.remove("fa-shake");
        icons[1].classList.add("disable");
        icons[0].classList.remove("disable");

        music.querySelector('.index').style.color = "black";
        music.querySelector('i').style.color = "rgba(0,0,0)";
        music.querySelector('i').style.cursor = "cursor";
    }, 500);

    document.querySelector(".section-selected .items").removeChild(s_music);
}

const getMyPlaylist = function () {
    let myPlaylist = "";
    let html = "";
    if (playlistList) {
        for (let e in playlistList) {
            let playlist = e;
            let playlistId = playlistList[e];
            myPlaylist += `<option value=${playlistId}>${e}</option>`
        }
    } else {
        html =
            `
        <form action="#">
            <select name="PLI" class="selectPlaylist">
                <option value="X">로그인 필요</option>
            </select>
        </form>
        `
        return html;
    }
    html =
        `
        <form action="#">
            <select name="PLI" class="selectPlaylist">
                <option value="X">재생목록</option>
                ${myPlaylist}
            </select>
        </form>
    `
    return html;
}

const checkJP = function(e){
    if(e.dataset.check==='N'){
        e.dataset.check='Y';
        e.querySelector("i").classList.remove("fa-square");
        e.querySelector("i").classList.add("fa-square-check");
    }else if(e.dataset.check==='Y'){
        e.dataset.check='N';
        e.querySelector("i").classList.add("fa-square");
        e.querySelector("i").classList.remove("fa-square-check");
    }
}

const submitSelection = async function(){
    const items = document.querySelectorAll(".section-selected .item");
    const resultSectionItems = document.querySelector(".section-result .items");
    let data = {};
    let postData = {};
    for(let i = 0;i<items.length;i++){
        let item = items[i];
        
        let videoId = item.dataset.ytId;
        let ytTitle = item.dataset.ytTitle;
        let ytChannel = item.dataset.ytChannel;

        let playlistId = item.querySelector(".selectPlaylist > option:checked").value;
        let playlistName = item.querySelector(".selectPlaylist > option:checked").innerHTML;
        let selectId = item.id;

        if(!videoId || playlistId==='X'){
            alert('작업이 완료된 후 눌러주세요.');
            return;
        }
        data[i] = {"selectId":selectId,"playlistName":playlistName};
        postData[i] = {"videoId": videoId,"ytTitle":ytTitle,"ytChannel":ytChannel, "playlistId": playlistId};
    }
    try {
        const loading = document.querySelector(".section-selected .loading");
        loading.classList.remove("disable");
        const response = await fetch(`/submitSelection`, {
            method: 'POST',
            body: JSON.stringify(postData)
        });
        if (!response.ok) {
            throw new Error('서버 응답이 실패했습니다.');
        }
        const flag = await response.json();
        for(let i = 0;i<flag.length;i++){
            if(flag[i]===1){
                //성공item.
                let sitem = document.getElementById(data[i].selectId);
                let thumbSrc = sitem.querySelector(".thumbnail-section .thumbnail").src;
                let link = sitem.querySelector('.title a').href;
                let title = sitem.querySelector('.title a').innerHTML;
                let ytTitle = sitem.querySelector(".title-container .yt-title").innerHTML;
                let index = data[i].selectId.split("-")[1];
                let playlistName = data[i].playlistName;

                resultSectionItems.innerHTML +=
                `
                <div class='item' id='r_music-${index}'>
                    <div class='title-section'>
                        <div class='index'><span>${index}</span></div>
                        <div class='thumbnail-section'>
                            <div><img class='thumbnail' src=${thumbSrc}></img></div>
                        </div>
                        <div class='title-container'>
                            <div class='title'>
                                <a href="${link}" target="_blank">
                                    ${title}
                                </a>
                            </div>
                            <div class='yt-title'>${ytTitle}</div>
                        </div>
                    </div>
                    <div class='result'>
                        <a href='https://www.youtube.com/watch?v=${postData[i].videoId}&list=${postData[i].playlistId}' target="_blank">
                            <span><i class="fa-solid fa-bars"></i></span>
                            <span>${playlistName}</span>   
                        </a>     
                    </div>
                </div>  
                `
                sitem.remove();
            }else if(flag[i]===2){
                //실패

            }
        }
        loading.classList.add("disable");
        swiper.slideTo(2);
        let checkedItem = document.querySelectorAll(".section-playlist-show .items .item");
        for(let i = 0;i<checkedItem.length;i++){
            if(checkedItem[i].dataset.check=='true'){
                // <i class="fa-regular fa-square-check"></i>
                const icons = checkedItem[i].querySelectorAll('.user-section');
                icons[1].classList.add("disable");
                icons[2].classList.remove("disable");
            }
        }
        document.querySelector(".survey").style.transform = "translate(0, -250px)";
    } catch (error) {
        console.error('DB 호출 중 오류가 발생했습니다.', error);
    } finally {
    }
}

const vote = async function(type){
    let s = document.querySelectorAll(".survey .icon span");
    
    for(let i = 0;i<s.length;i++){
        s[i].style.cursor = "auto";
        if(s[i].dataset.enable === "N") return;
    }
    if(type===0){
        // LIKE
       
    }else{
        // DISLIKE
        
        const s = document.querySelector(".survey-user-opinion");
        let sides = document.querySelectorAll(".survey-main-side");
        sides[0].style.width = 0;
        sides[0].innerHTML = "";
        sides[1].style.width = 0;
        sides[1].innerHTML = "";
        s.style.width = "330px";
        document.querySelector(".survey-main .like").style.width = 0;
        document.querySelector(".survey-main .blank").style.width = 0;
        setTimeout(function(){
            document.querySelector(".survey-main .like").classList.add("disable");
            s.classList.remove("disable");
            document.querySelector(".btn-survey").classList.remove("disable");
        },1000);        
    }

    const response = await fetch(`/vote`, {
        method: 'POST',
        body: type
    });
    if (!response.ok) {
        throw new Error('서버 응답이 실패했습니다.');
    }
    const id = await response.json();
    if(type===0) {
        document.querySelector(".survey-footer .thx").classList.remove("disable");
        surveyDisappear(); 
    }
    document.querySelector(".survey-user-opinion").dataset.id = id;

    
}
const checkVote = function(checkbox){
    let s = checkbox.querySelectorAll("i");
    if(checkbox.dataset.enable === "N") return;
    s[0].classList.toggle("disable");
    s[1].classList.toggle("disable");
    if(checkbox.parentNode.dataset.check==="N"){
        checkbox.parentNode.dataset.check = "Y";
    }else{
        checkbox.parentNode.dataset.check = "N";
    }
}
const clearLink = function(){
    document.getElementById("link").value="";
}

const submitSurvey = async function(){
    let s = document.querySelectorAll(".survey-user-opinion div");
    let t = document.querySelector(".survey-user-opinion").dataset.id+"#";
    for(let i = 0;i<s.length;i++){
        if(s[i].dataset.check==="Y"){
            t = t+"0#";
            if(i===2){
                t = t + s[i].querySelector("input").value+"#";
            }
        }else{
            t = t+"1#";
        }
    }
    
    const response = await fetch(`/voteDetail`, {
        method: 'POST',
        body: t
    });
    if (!response.ok) {
        throw new Error('서버 응답이 실패했습니다.');
    }
    const flag = await response.json(); 
    document.querySelector(".survey-footer .thx").classList.remove("disable");
    let r = document.querySelectorAll(".survey-main .check-box");
    for(let i =0;i<r.length;r++){
        r[i].dataset.enable = "N";
    }
    document.querySelector(".input-etc").readOnly = true;
    surveyDisappear();
}   
const surveyDisappear = function(){
    setTimeout(function(){
        document.querySelector(".survey").style.transform = "translate(0, 250px)";
        setTimeout(function(){
            document.querySelector(".survey").innerHTML =
            `<div class="survey-header">SURVEY</div>
            <div class="survey-main">
                <div class="survey-main-side"><span>추천</span></div>
                <div class="icon like">
                    <span onclick="vote(0)" data-enable="Y">
                        <i class="fa-regular fa-thumbs-up"></i>
                    </span>
                </div>
                <div class="blank" style="width: 20px;"></div>
                <div class="icon dislike">
                    <span onclick="vote(1)" data-enable="Y">
                        <i class="fa-regular fa-thumbs-up fa-rotate-180"></i>
                    </span>
                </div>
                <div class="survey-main-side"><span>비추천</span></div>
                <div class="survey-user-opinion disable">
                    <div data-check="N">
                        <span class="check-box" onclick="checkVote(this)" data-enable="Y">
                            <i class="fa-regular fa-square"></i>
                            <i class="fa-regular fa-square-check disable"></i>
                        </span>
                        <span>부정확함</span>
                    </div>
                    <div data-check="N">
                        <span class="check-box" onclick="checkVote(this)" data-enable="Y">
                            <i class="fa-regular fa-square"></i>
                            <i class="fa-regular fa-square-check disable"></i>
                        </span>
                        <span>불편함</span>
                    </div>
                    <div data-check="N">
                        <span class="check-box" onclick="checkVote(this)" data-enable="Y">
                            <i class="fa-regular fa-square"></i>
                            <i class="fa-regular fa-square-check disable"></i>
                        </span>
                        <span>기타</span>
                        <input class="input-etc">
                    </div>
                </div>
            </div>
            <div class="survey-footer">
                <div>
                    <span class="thx disable">감사합니다.</span>
                </div>
                <div>
                    <div class="btn-survey disable" onclick="submitSurvey()">제&nbsp;출</div>
                </div>
            </div>`
        })
    }, 2000); 
    
}

const refreshPlaylist = async function(refresh){
    
    if(refresh.classList.contains("timeout")){
        return;
    }
    else{
        refresh.classList.add("timeout");
    }

    const response = await fetch(`/refreshPlaylist`, {
        method: 'POST'
    });
    if (!response.ok) {
        throw new Error('서버 응답이 실패했습니다.');
    }
    const flag = await response.json();
    let html = "";
    for(let i=0;i<flag.length;i++){
        let name = flag[i][0];
        let id = flag[i][1];
        html +=`
            <div class="playlist">
                <a href="https://www.youtube.com/playlist?list=${id}" target="_blank">
                    <div><span>${i+1}. </span></div>
                    <div>${name}</div>
                </a>
            </div>
        `;
    }
    document.querySelector("#playlist .content").innerHTML = html;
    setTimeout(function(){
        let s = document.querySelector("#playlist .footer span");
        s.classList.remove("timeout");
    },1000 * 60);
}

const expandUserMenu = function(element){
    let s = document.querySelectorAll(".section-member-list");
    switch(element.dataset.status){
        case "closed":
            s.forEach(function(e){
                e.classList.add("open");
                e.querySelector(".expand").classList.add("fa-rotate-180");
            });
            element.dataset.status = "open";
            break;
        case "open":
            s.forEach(function(e){
                e.classList.remove("open");
                e.querySelector(".expand").classList.remove("fa-rotate-180");
            });
            element.dataset.status = "closed";
            break;
    }
    

}

const fillLink = function(link){
    const input = document.getElementById("link");
    input.value = link;
}