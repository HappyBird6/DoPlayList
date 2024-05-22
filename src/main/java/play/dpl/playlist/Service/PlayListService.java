package play.dpl.playlist.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import io.github.bonigarcia.wdm.WebDriverManager;
import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Entity.Music;
import play.dpl.playlist.Entity.Playlist;
import play.dpl.playlist.Entity.PlaylistRequestHistory;
import play.dpl.playlist.Repository.PlaylistRepository;
import play.dpl.playlist.Repository.PlaylistRequestHistoryRepository;

@Service
public class PlaylistService {
    
    @Autowired
    private PlaylistRepository playlistRepository;
    @Autowired
    private PlaylistRequestHistoryRepository playlistRequestHistoryRepository;


    private List<String> scrapChapter(ChromeDriver driver) {
        // system.out.println("챕터 스크랩 시작");
        List<String> titles = new ArrayList<>();
        List<WebElement> chapters = driver.findElements(
                By.cssSelector("#details > h4.macro-markers.style-scope.ytd-macro-markers-list-item-renderer"));
        if (!chapters.isEmpty()) {
            for (var chapter : chapters) {
                String title = chapter.getAttribute("title");
                
                title = removeSpecialCharacters(title).trim();
                if (!titles.contains(title) && !title.isEmpty()) {
                    titles.add(title);
                }
            }
        } else {
            // system.out.println("챕터 없음");
            return null;
        }
        return titles;
    }

    private List<String> scrapExpand(ChromeDriver driver) {
        // system.out.println("더보기란 타임스탬프 스크랩 시작");
        List<String> titles = new ArrayList<>();
        List<WebElement> videoDetails = driver
                .findElements(By.cssSelector("#description-inline-expander > yt-attributed-string > span > span"));
        if (!videoDetails.isEmpty()) {
            boolean isTitle = false;
            for (var detail : videoDetails) {
                try {
                    WebElement detailLink = detail.findElement(By.tagName("a"));
                    // 링크가 있다
                    if (containsTimeStamp(detailLink.getText())) {
                        // 타임스탬프
                        isTitle = true;
                    }
                } catch (Exception e) {
                    if (isTitle) {
                        String title = detail.getText();
                        if (title.contains("\n")) {
                            title = title.substring(0, title.indexOf("\n"));
                        }
                        title = removeSpecialCharacters(title).trim();
                        if (!titles.contains(title) && !title.isEmpty()) {
                            titles.add(title);
                        }
                        isTitle = false;
                    }
                }
            }
        } else {
            // system.out.println("더보기란 없음");
            return null;
        }
        if(titles.isEmpty()) {
            // system.out.println("더보기란 타임스탭프 없음");
            return null;
        }
        // System.out.println(titles.toString());
        return titles;
    }

    private List<String> scrapFirstComment(ChromeDriver driver) {
        // system.out.println("첫 댓글 타임스탬프 스크랩 시작");
        WebElement element;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<String> titles = new ArrayList<>();
        List<WebElement> firstCommentContent = driver
                .findElements(By.cssSelector("#content-text > yt-formatted-string"));
        int count = 0;
        while (count < 10) {
            System.out.println("COUNT : "+count);
            try {
                js.executeScript("window.scrollBy(0,300)");
                Thread.sleep(1000);
                // element = driver
                //         .findElement(By.cssSelector("#contents > ytd-comment-thread-renderer:nth-child(1)"));
                //         System.out.println("ELEMENT : "+element.getTagName());
                //         System.out.println("ELEMENT : "+element.getLocation().toString());
                //         // element.findElement(By.cssSelector("#more > span")).click();
                // Thread.sleep(100);
                // // firstCommentContent = element.findElements(By.className("yt-formatted-string"));
                // firstCommentContent = element.findElements(By.cssSelector("#content-text > .span"));
                // System.out.println("firstCommentContent : "+firstCommentContent.toString());
                firstCommentContent = driver.findElements(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-comments/ytd-item-section-renderer/div[3]/ytd-comment-thread-renderer[1]/ytd-comment-view-model/div[3]/div[2]/ytd-expander/div/yt-attributed-string/span"));
                
                if (!firstCommentContent.isEmpty()){
                    break;
                }
            } catch (Exception e) {
            } finally {
                count++;
            }
        }
        if (!firstCommentContent.isEmpty()) {
            String text = firstCommentContent.get(0).getText();
            // System.out.println(text);
            int s_index = 0;
            int e_index = 0;
            int cnt = 0;
            boolean isStop = false;
            while(cnt<1000){
                e_index = text.indexOf("\n",s_index);
                if(e_index==-1) {
                    isStop = true;
                }
                
                try{
                    String title = "";
                    if(!isStop) {
                        title = text.substring(s_index, e_index);
                    }else{
                        title = text.substring(s_index,text.length());
                    } 
                    // System.out.println("title : "+title);
                    if(!title.isEmpty() && containsTimeStamp(title)){
                        // System.out.println("타임스탬프 발견");
                        title = removeTimeStamps(title).trim();
                        // System.out.println("바뀐 title : "+title);
                    }
                    title = standardize(title);
                    if (!titles.contains(title.trim()) && !title.isEmpty()) {
                        if(isTitle(title.trim())) titles.add(title.trim());
                    }
                    // System.out.println(title);
                    s_index = e_index+1;

                }catch(Exception e){
                    break;
                }
                cnt++;
                if(isStop) break;
            }
            // for (var line : firstCommentContent) {
            //     if (line.getTagName().equals("a")) {
            //         // 링크
            //         if (isTimeStamp(line.getText())) {
            //             isTitle = true;
            //         }
            //     } else {
            //         if (isTitle) {
            //             String title = line.getText();
            //             if (title.contains("\n")) {
            //                 title = title.substring(0, title.indexOf("\n"));
            //             }
                        
            //             title = removeSpecialCharacters(title).trim();
            //             if (!titles.contains(title) && !title.isEmpty()) {
            //                 titles.add(title);
            //             }
            //             isTitle = false;
            //         }
            //     }
            // }
        } else {
            // system.out.println("첫 댓글 타임스탬프 없음");
            return null;
        }
        return titles;
    }

    private List<String> scrapMusic(ChromeDriver driver) {
        // system.out.println("음악 섹션 스크랩 시작");
        WebElement element;
        List<String> titles = new ArrayList<>();
        try{
            while (true) {
                List<WebElement> musicElements = driver
                        .findElements(By.cssSelector("#items > ytd-video-attribute-view-model"));
                if (!musicElements.isEmpty()) {
                    for (var music : musicElements) {
                        String title = music.findElement(By.cssSelector(
                                "video-attribute-view-model-c3 > div > a > div.yt-video-attribute-view-model__metadata > h3"))
                                .getText();
                        String singer = music.findElement(By.cssSelector(
                                "video-attribute-view-model-c3 > div > a > div.yt-video-attribute-view-model__metadata > h4"))
                                .getText();
                        String tmp = removeSpecialCharacters(singer + " - " + title);
                        title = title.trim();
                        if (!titles.contains(tmp) && !title.isBlank() && !singer.isBlank() && !title.isEmpty()) {
                            titles.add(tmp);
                        }
                    }
                }
                try{
                    element = driver.findElement(By.xpath(
                        "/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-watch-metadata/div/div[4]/div[1]/div/ytd-text-inline-expander/div[2]/ytd-structured-description-content-renderer/div/ytd-horizontal-card-list-renderer[2]/div[2]/div[3]"));
                    if (element.isDisplayed()) {
                        WebElement nextBtn = element.findElement(By.xpath(
                                "/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-watch-metadata/div/div[4]/div[1]/div/ytd-text-inline-expander/div[2]/ytd-structured-description-content-renderer/div/ytd-horizontal-card-list-renderer[2]/div[2]/div[3]/div[1]"));
                        nextBtn.click();
                    } else {
                        break;
                    }
                }catch(Exception e){
                    break;
                }
                
            }
        }catch(Exception e){
            // system.out.println("음악 섹션 없음");
            return null;
        }
        
        if(titles.isEmpty()) {
            // system.out.println("음악 섹션 없음");
            return null;
        }
        return titles;
    }
    
    private Playlist getData(ChromeDriver driver) throws InterruptedException{
        Playlist playlist = new Playlist();
        while(true){
            try{
                playlist.setYtChannel(driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-watch-metadata/div/div[2]/div[1]/ytd-video-owner-renderer/div[1]/ytd-channel-name/div/div/yt-formatted-string/a")).getText());
                playlist.setYtTitle(driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-watch-metadata/div/div[1]/h1/yt-formatted-string")).getText());
                playlist.setYtPostDate(driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-watch-flexy/div[5]/div[1]/div/div[2]/ytd-watch-metadata/div/div[4]/div[1]/div/ytd-watch-info-text/div/yt-formatted-string/span[3]")).getText());
                break;
            }catch(Exception e){
                Thread.sleep(100);
            }
        }
        return playlist;          
    }
    private String isOneSinger(String title){
        List<String> keywords = Arrays.asList("노래 모음","노래모음");

        String singer = null;
        for(int i =0;i<keywords.size();i++){
            String keyword = keywords.get(i);
            // system.out.println("키워드 : "+keyword+"에 대한 검사");
            if(title.contains(keyword)){
                // system.out.println("타이틀이 키워드 --"+keyword+"-- 를 포함");
                int endIndex = title.indexOf(keyword);
                int startIndex = 0;
                boolean skip = true;
                for(int j = endIndex-1;j>0;j--){
                    char c = title.charAt(j);
                    // system.out.println(j+"인덱스의 캐릭터 : "+c);
                    if(!Character.isWhitespace(c) && Character.isLetterOrDigit(c)) skip = false;

                    if(Character.isWhitespace(c) || !Character.isLetterOrDigit(c)) {
                        if(skip) continue;
                        startIndex = j;
                        break;
                    }
                }
                singer = title.substring(startIndex, endIndex).trim();
                break;
            }
        }
        return singer;
    }
    public Playlist scrapPage(String url) {
        // System.out.println("SCRAP PAGE : 드라이버 셋업 전");
        WebDriverManager.chromedriver().setup();
        // System.out.println("SCRAP PAGE : 드라이버 셋업 후");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--single-process");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
        WebElement element;
        Playlist playlist;
        
        List<String> titles;
        try {
            // WebDriverManager.chromedriver().create();
            driver.get(url);

            Thread.sleep(1000); // 페이지 로딩 대기시간
            // 1. 더 보기 클릭
            while (true) {
                try {
                    element = driver.findElement(By.id("expand"));
                    element.click();
                    break;
                } catch (Exception e) {
                    Thread.sleep(100);
                }
            }
            Thread.sleep(100);
            // 0. 기본 정보 체크
            playlist = getData(driver);
            // String singer = isOneSinger(playlist.getYtTitle());
            String singer = null;

            playlist.setId(extractVideoIdFromUrl(url));
            
            // 2. 챕터가 있으면 스크래핑
            titles = scrapChapter(driver);
            if (titles != null) {
                driver.quit();
                String commonPrefix = findCommonPrefix(titles,0);
                if(commonPrefix!=null){
                    titles = removeCommonPrefix(titles, commonPrefix);
                }
                // if(singer!=null){
                //     for(int i =0;i<titles.size();i++){
                //         titles.set(i,singer+" - "+titles.get(i));
                //     }
                // }
                String titlesJson = new Gson().toJson(titles);
                playlist.setYtMusicTitlesJson(titlesJson);
                return playlist;
            }
            // 3. 더보기란에 주인장이 올린 타임라인 분석
            titles = scrapExpand(driver);
            if (titles != null) {
                driver.quit();
                String commonPrefix = findCommonPrefix(titles,0);
                if(commonPrefix!=null){
                    titles = removeCommonPrefix(titles, commonPrefix);
                }
                // if(singer!=null){
                //     for(int i =0;i<titles.size();i++){
                //         titles.set(i,singer+" - "+titles.get(i));
                //     }
                // }
                String titlesJson = new Gson().toJson(titles);
                playlist.setYtMusicTitlesJson(titlesJson);
                return playlist;
            }
            // 4. 첫번째 댓글 타임라인 분석
            titles = scrapFirstComment(driver);
            if (titles != null) {
                driver.quit();
                String commonPrefix = findCommonPrefix(titles,0);
                if(commonPrefix!=null){
                    titles = removeCommonPrefix(titles, commonPrefix);
                }
                // if(singer!=null){
                //     for(int i =0;i<titles.size();i++){
                //         titles.set(i,singer+" - "+titles.get(i));
                //     }
                // }
                String titlesJson = new Gson().toJson(titles);
                playlist.setYtMusicTitlesJson(titlesJson);
                if(titles.isEmpty()) return null;
                return playlist;
            }
            // 5. 더보기 안의 음악란에만 표시된 음악만 표기
            titles = scrapMusic(driver);
            if (titles != null) {
                driver.quit();
                String commonPrefix = findCommonPrefix(titles,0);
                if(commonPrefix!=null){
                    titles = removeCommonPrefix(titles, commonPrefix);
                }
                // if(singer!=null){
                //     for(int i =0;i<titles.size();i++){
                //         titles.set(i,singer+" - "+titles.get(i));
                //     }
                // }
                String titlesJson = new Gson().toJson(titles);
                playlist.setYtMusicTitlesJson(titlesJson);
                return playlist;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }
        return null;
    }

    // 타임스탬프 패턴
    private static final String TIME_STAMP_PATTERN = "(?:[01]?\\d|2[0-3]):?[0-5]\\d(?:[0-5]\\d)?";
    // 타임스탬프 ~ 타임스탬프 형식의 패턴
    private static final String FULL_PATTERN = TIME_STAMP_PATTERN + "(?:~" + TIME_STAMP_PATTERN + ")?";
    private static boolean containsTimeStamp(String input) {
        // String pattern = "([0-5]?[0-9]|6[0-9])?(:[0-5]?[0-9](:[0-5]?[0-9])?)?";
        // // Pattern 클래스를 사용하여 정규표현식을 컴파일
        // Pattern regexPattern = Pattern.compile(pattern);

        // // Matcher 클래스를 사용하여 입력된 문자열과 패턴을 비교
        // Matcher matcher = regexPattern.matcher(input);

        // // 일치 여부 반환
        // return matcher.matches();

        Pattern regexPattern = Pattern.compile(FULL_PATTERN);
        Matcher matcher = regexPattern.matcher(input);
        return matcher.find();
    }
    public static String removeTimeStamps(String input) {
        Pattern regexPattern = Pattern.compile(FULL_PATTERN);
        Matcher matcher = regexPattern.matcher(input);
        return matcher.replaceAll("");
    }

    public String extractVideoIdFromUrl(String url) {
        // https://www.youtube.com/watch?v=fDxVNLUG-K8&list=WL&index=3&t=1049s
        // https://youtu.be/xbc6eDHmXkE?si=5Iv50p8z5Mk59WU8
        
        String videoId = null;
        int startIndex = 0;
        int endIndex = -1;
        int addIndex = 0;
        if(url.contains("youtu.be/")){
            startIndex = url.indexOf("youtu.be/");
            endIndex = url.indexOf("?",startIndex);
            addIndex = 9;
        }else{
            startIndex = url.indexOf("?v=");
            endIndex = url.indexOf("&", startIndex);
            addIndex = 3;
        }
        if(startIndex==0) return null;
        if(endIndex==-1){
            endIndex = url.length();
        }

        videoId = url.substring(startIndex + addIndex, endIndex);

        return videoId;
    }

    public Music getMusicData(String title, String type){
        /*
         * type
         * 0 : default
         * 1 : 일식
         */
        String suffix = "";
        if(type.equals("0")){
            suffix = "\"Auto-generated\"";
        }else if(type.equals("1")){
            suffix = "\"Official\"";
        }

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--single-process");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
        
        WebElement element;
        Music music = null;
        try {
            // WebDriverManager.chromedriver().create();
            driver.get("http://www.youtube.com/results?search_query="+title.trim()+" "+suffix+"&sp=CAM%253D");

            Thread.sleep(1000); // 페이지 로딩 대기시간
            Long s = new Date().getTime();
            while (true) {
                Long e = new Date().getTime();
                if(e-s>5000) break;
                try {
                    element = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer/div[3]/ytd-video-renderer[1]/div[1]/ytd-thumbnail/a"));
                    String link = element.getAttribute("href");
                    String videoId = extractVideoIdFromUrl(link);
                    if(videoId==null) return null;
                    element = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer/div[3]/ytd-video-renderer[1]/div[1]/div/div[1]/div/h3/a/yt-formatted-string"));
                    String ytTitle = element.getText();
                    element = driver.findElement(By.xpath("/html/body/ytd-app/div[1]/ytd-page-manager/ytd-search/div[1]/ytd-two-column-search-results-renderer/div/ytd-section-list-renderer/div[2]/ytd-item-section-renderer/div[3]/ytd-video-renderer[1]/div[1]/div/div[2]/ytd-channel-name/div/div/yt-formatted-string/a"));
                    String ytChannel = element.getText();
                    
                    music = Music.builder()
                                    .id(videoId)
                                    .ytChannel(ytChannel)
                                    .ytTitle(ytTitle)
                                    .build();

                    // 썸네일 데이터 추가 : "https://i1.ytimg.com/vi/"+videoId+"/default.jpg"
                    // 링크 : "https://www.youtube.com/watch?v="+videoId
                    
                    break;
                } catch (Exception err) {
                    Thread.sleep(100);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }

        return music;
    }
   
    public Playlist getPlaylist(String url){
        String videoId = extractVideoIdFromUrl(url);

        // 플리 저장
        Playlist playlist = new Playlist();
        Optional<Playlist> temp = playlistRepository.findById(videoId);
        if(temp.isPresent()){
            playlist = temp.get();
            playlistRepository.updateRequestCount(playlist.getId());
        }else{
            playlist = scrapPage(url);
            playlist.setRequestCount(1l);
            playlistRepository.save(playlist);
        }
        // 리퀘스트 저장
        if (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getMember();
            PlaylistRequestHistory prh = new PlaylistRequestHistory();
            Optional<Long> tempMaxId = playlistRequestHistoryRepository.findMaxId();
            if(tempMaxId.isPresent()){
                prh.setId(tempMaxId.get()+1);
            }else{
                prh.setId(1l);
            }
            prh.setMemberId(member.getEmail());
            prh.setPlaylistId(videoId);
            prh.setRequestAt(new Date());
            
            playlistRequestHistoryRepository.save(prh);
        }   
        
        return playlist; 
    }

    private String findCommonPrefix(List<String> titles,int depth) {
        if(titles==null || titles.isEmpty()) return null;
        // // system.out.println(titles.toString());
        String shortest = titles.get(0).substring(depth,depth+1);
        for (String title : titles) {
            if(!title.substring(depth,depth+1).equals(shortest)){ 
                return null;
            }
        }
        String s = findCommonPrefix(titles, depth+1);
        shortest = s==null ? shortest : shortest +s;

        return shortest;
    }
    private static List<String> removeCommonPrefix(List<String> titles, String prefix) {
        List<String> res = new ArrayList<>();
        for(var e : titles){
            res.add(e.replace(prefix, ""));
        }
        return res;
    }
    public List<String> addCommonSuffix(List<String> titles){
        List<String> res = new ArrayList<>();
        for(var e : titles){
            res.add(e + " Auto-generated");
        }
        return res;
    }
    public boolean isTitle(String title){
        boolean isTitle = true;
        title = title.toLowerCase();
        boolean A = title.contains("타임라인");
        boolean B = title.contains("타 임 라 인");
        boolean C = title.contains("타임 라인");
        boolean D = title.contains("tracklist");
        boolean E = title.contains("track list");
        boolean F = title.contains("timeline");
        boolean G = title.contains("time line");
        if(A || B || C || D || E || F || G) isTitle = false;
        return isTitle;
    }
    public String standardize(String title){
        title = title.replace("~","");
        title = title.replace(":","");
        title = title.replace("[","");
        title = title.replace("]","");
        return title;
    }
    private static String removeSpecialCharacters(String input){

        String cleanedString = input.replaceAll("[^\\p{L}\\p{N} ,\\-_&$!*{}\\[\\]()]+", "");
        cleanedString = cleanedString.trim();

        return cleanedString;
    }
}
