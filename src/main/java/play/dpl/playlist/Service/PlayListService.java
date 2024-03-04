package play.dpl.playlist.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import io.github.bonigarcia.wdm.WebDriverManager;

@Service
public class PlayListService {
    @Value("${youtube.data.api.key}")
    String youtubeAPIkey;

    private List<String> scrapChapter(ChromeDriver driver) {
        System.out.println("챕터 스크랩 시작");
        List<String> titles = new ArrayList<>();
        List<WebElement> chapters = driver.findElements(
                By.cssSelector("#details > h4.macro-markers.style-scope.ytd-macro-markers-list-item-renderer"));
        if (!chapters.isEmpty()) {
            for (var chapter : chapters) {
                String title = chapter.getAttribute("title");
                titles.add(removeSpecialCharacters(title));
            }
        } else {
            System.out.println("챕터 없음");
            return null;
        }
        return titles;
    }

    private List<String> scrapExpand(ChromeDriver driver) {
        System.out.println("더보기란 타임스탬프 스크랩 시작");
        List<String> titles = new ArrayList<>();
        List<WebElement> videoDetails = driver
                .findElements(By.cssSelector("#description-inline-expander > yt-attributed-string > span > span"));
        if (!videoDetails.isEmpty()) {
            boolean isTitle = false;
            for (var detail : videoDetails) {
                try {
                    WebElement detailLink = detail.findElement(By.tagName("a"));
                    // 링크가 있다
                    if (isTimeStamp(detailLink.getText())) {
                        // 타임스탬프
                        isTitle = true;
                    }
                } catch (Exception e) {
                    if (isTitle) {
                        String title = detail.getText();
                        if (title.contains("\n")) {
                            title = title.substring(0, title.indexOf("\n"));
                        }
                        if (!titles.contains(removeSpecialCharacters(title))) {
                            titles.add(removeSpecialCharacters(title));
                        }
                        isTitle = false;
                    }
                }
            }
        } else {
            System.out.println("더보기란 없음");
            return null;
        }
        if(titles.isEmpty()) {
            System.out.println("더보기란 타임스탭프 없음");
            return null;
        }
        return titles;
    }

    private List<String> scrapFirstComment(ChromeDriver driver) {
        System.out.println("첫 댓글 타임스탬프 스크랩 시작");
        WebElement element;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<String> titles = new ArrayList<>();
        List<WebElement> firstCommentContent = driver
                .findElements(By.cssSelector("#content-text > yt-formatted-string"));
        int count = 0;
        while (count < 10) {
            try {
                js.executeScript("window.scrollBy(0,500)");
                Thread.sleep(500);
                element = driver
                        .findElement(By.cssSelector("#contents > ytd-comment-thread-renderer:nth-child(1)"));
                element.findElement(By.cssSelector("#more > span")).click();
                Thread.sleep(50);
                firstCommentContent = element.findElements(By.className("yt-formatted-string"));
                if (!firstCommentContent.isEmpty())
                    break;
            } catch (Exception e) {
            } finally {
                count++;
            }
        }
        if (!firstCommentContent.isEmpty()) {
            boolean isTitle = false;
            for (var line : firstCommentContent) {
                if (line.getTagName().equals("a")) {
                    // 링크
                    if (isTimeStamp(line.getText())) {
                        isTitle = true;
                    }
                } else {
                    if (isTitle) {
                        String title = line.getText();
                        if (title.contains("\n")) {
                            title = title.substring(0, title.indexOf("\n"));
                        }
                        if (!titles.contains(removeSpecialCharacters(title))) {
                            titles.add(removeSpecialCharacters(title));
                        }
                        isTitle = false;
                    }
                }
            }
        } else {
            System.out.println("첫 댓글 타임스탬프 없음");
            return null;
        }
        return titles;
    }

    private List<String> scrapMusic(ChromeDriver driver) {
        System.out.println("음악 섹션 스크랩 시작");
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
                        
                        if (!titles.contains(tmp) && !title.isBlank() && !singer.isBlank()) {
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
            System.out.println("음악 섹션 없음");
            return null;
        }
        
        if(titles.isEmpty()) {
            System.out.println("음악 섹션 없음");
            return null;
        }
        return titles;
    }

    public List<String> scrapPage(String url) {
        ChromeDriver driver = null;
        WebElement element;
        List<String> titles;
        try {
            WebDriverManager.chromedriver().create();
            driver = new ChromeDriver();


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
            // 2. 챕터가 있으면 스크래핑
            titles = scrapChapter(driver);
            if (titles != null) {
                driver.quit();
                return titles;
            }
            // 3. 더보기란에 주인장이 올린 타임라인 분석
            titles = scrapExpand(driver);
            if (titles != null) {
                driver.quit();
                return titles;
            }
            // 4. 첫번째 댓글 타임라인 분석
            titles = scrapFirstComment(driver);
            if (titles != null) {
                driver.quit();
                return titles;
            }
            // 5. 더보기 안의 음악란에만 표시된 음악만 표기
            titles = scrapMusic(driver);
            if (titles != null) {
                driver.quit();
                return titles;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 브라우저 종료
            driver.quit();
        }
        return null;
    }

    private static boolean isTimeStamp(String input) {
        String pattern = "([0-5]?[0-9]|6[0-9])?(:[0-5]?[0-9](:[0-5]?[0-9])?)?";
        // Pattern 클래스를 사용하여 정규표현식을 컴파일
        Pattern regexPattern = Pattern.compile(pattern);

        // Matcher 클래스를 사용하여 입력된 문자열과 패턴을 비교
        Matcher matcher = regexPattern.matcher(input);

        // 일치 여부 반환
        return matcher.matches();
    }
    private static String removeSpecialCharacters(String input){

        String cleanedString = input.replaceAll("[^\\p{L}\\p{N} ,\\-_&$!*{}\\[\\]()]+", "");
        cleanedString = cleanedString.trim();

        return cleanedString;
    }
    public String requestCaptionsId(String videoId) {
        String captionsId = null;
        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/captions";
            URI uri = URI.create(apiUrl + "?part=snippet&videoId=" + videoId + "&key=" + youtubeAPIkey);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseData = response.body();
                // JSON 파싱
                JSONObject json = new JSONObject(responseData);

                // "items" 배열에서 첫 번째 아이템을 가져옴
                JSONArray itemsArray = json.getJSONArray("items");
                JSONObject firstItem = itemsArray.getJSONObject(0);

                // "id" 값을 추출
                captionsId = firstItem.getString("id");

                System.out.println("Response: " + responseData);
            } else {
                System.out.println("Failed to retrieve video information. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("CaptionsId : " + captionsId);
        return captionsId;
    }

    public void requestCaptions(String captionsId) {
        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/captions/" + captionsId + "?key=" + youtubeAPIkey;
            URI uri = URI.create(apiUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseData = response.body();
                System.out.println("Response: " + responseData);
            } else {
                System.out.println("Failed to retrieve video information. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestVideoData(String videoId) {
        // String videoId = "fDxVNLUG-K8";
        // API 요청을 보내고 응답을 처리합니다.
        System.out.println("requestVideoData / videoId : " + videoId);
        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/videos";
            URI uri = URI.create(apiUrl + "?part=snippet,contentDetails&id=" + videoId + "&key=" + youtubeAPIkey);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseData = response.body();
                System.out.println("Response: " + responseData);
            } else {
                System.out.println("Failed to retrieve video information. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String extractVideoIdFromUrl(String url) {
        // https://www.youtube.com/watch?v=fDxVNLUG-K8&list=WL&index=3&t=1049s
        String videoId = null;
        int startIndex = url.indexOf("?v=");
        int endIndex = url.indexOf("&", startIndex);
        if (endIndex == -1)
            endIndex = url.length();

        videoId = url.substring(startIndex + 3, endIndex);

        return videoId;
    }
}
