class AsyncTaskQueue {
    constructor() {
        this.queue = [];
        this.isProcessing = false;
    }

    enqueue(asyncTask) {
        this.queue.push(asyncTask);
        if (!this.isProcessing) {
            this.processQueue();
        }
    }

    async processQueue() {
        this.isProcessing = true;
        while (this.queue.length > 0) {
            const asyncTask = this.queue.shift();
            try {
                await asyncTask();
            } catch (error) {
                console.error('비동기 작업 실패:', error);
            }
        }
        this.isProcessing = false;
    }
}
const asyncTaskQueue = new AsyncTaskQueue();

function fetchMusicData(index,title,type){
    let item = document.getElementById(`s_music-${index}`);
    if(!item) { return; }

    
    return new Promise(async (resolve,reject)=>{
        try {
            const response = await fetch(`/getMusicData`, {
                method: 'POST',
                body: (title + "#" + type)
            });
            if (!response.ok) {
                throw new Error('서버 응답이 실패했습니다.');
            }
            const data = await response.json();

            item = document.getElementById(`s_music-${index}`);
            let loading = item.querySelector(".thumbnail-section .loading");
            item.dataset.ytId = data.id;
            item.dataset.ytChannel = data.ytChannel;
            item.dataset.ytTitle = data.ytTitle;

            item.querySelector(".thumbnail-section .thumbnail").src = "https://i1.ytimg.com/vi/"+data.id+"/default.jpg"
            item.querySelector(".title-container .yt-title").innerHTML = `<a href='https://www.youtube.com/watch?v=${data.id}' target='_blank'>[${data.ytChannel}] ${data.ytTitle}</a>`;
            loading.classList.add("disable");
            if(step!==2) document.querySelector(".exclamation").classList.remove("disable");
            resolve(data);
        } catch (error) {
            reject(error);
        }
    })
}
// 비동기 작업을 큐에 추가하는 함수
function enqueueAsyncTask(index,title,type) {
    const asyncTask = async () => {
        try {
            return await fetchMusicData(index,title,type);
        } catch (error) {
            console.error(error);
        }
    };
    asyncTaskQueue.enqueue(asyncTask);
}