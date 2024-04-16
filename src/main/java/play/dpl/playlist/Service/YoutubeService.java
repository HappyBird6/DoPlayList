package play.dpl.playlist.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.gson.JsonObject;

import play.dpl.playlist.Entity.Member;
import play.dpl.playlist.Entity.Music;
import play.dpl.playlist.Entity.MusicRequestHistory;
import play.dpl.playlist.Entity.PlaylistRequestHistory;
import play.dpl.playlist.Repository.MusicRequestHistoryRepository;
import play.dpl.playlist.Repository.MusicRespository;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class YoutubeService {

    private static final String CLIENT_SECRETS= "/client-secrets/client-secrets.json";
    private static final Collection<String> SCOPES =
        Arrays.asList("https://www.googleapis.com/auth/youtube.force-ssl");

    private static final String APPLICATION_NAME = "DoPlayList";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Autowired
    private MusicRespository musicRespository;
    @Autowired
    private MusicRequestHistoryRepository musicRequestHistoryRepository;

    public static Credential authorize(final NetHttpTransport httpTransport,String accessToken) throws IOException {
        // // Load client secrets.
        InputStream in = YoutubeService.class.getResourceAsStream(CLIENT_SECRETS);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build();

        // GoogleTokenResponse 객체 생성
        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken(accessToken);

        // 액세스 토큰을 사용하여 Credential 객체를 만듭니다.
        Credential credential = flow.createAndStoreCredential(tokenResponse, null);
        return credential;
    }

    public static YouTube getService(String accessToken) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport,accessToken);
        
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public void addPlayList(String accessToken,String title, String description) 
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService(accessToken);
        
        // Define the Playlist object, which will be uploaded as the request body.
        Playlist playlist = new Playlist();
        
        // Add the id string property to the Playlist object.
        playlist.setId("");
        
        // Add the snippet object property to the Playlist object.
        PlaylistSnippet snippet = new PlaylistSnippet();
        snippet.setDefaultLanguage("ko");
        snippet.setDescription(description);
        snippet.setTitle(title);

        playlist.setSnippet(snippet);

        // Define and execute the API request
        YouTube.Playlists.Insert request = youtubeService.playlists()
            .insert("snippet", playlist);
        Playlist response = request.execute();
        System.out.println("addPlayList : " + response);
    }
    public List<String[]> getPlayList(String accessToken, String nextPageToken) throws GeneralSecurityException, IOException, GoogleJsonResponseException{
        List<String[]> list = new ArrayList<>();
        YouTube youtubeService = getService(accessToken);

        
        YouTube.Playlists.List request = youtubeService.playlists()
            .list("id,snippet");
        request.setFields("items(id,snippet(title)),pageInfo(totalResults,resultsPerPage),nextPageToken");
        request.setMaxResults(50L).setMine(true);
        

        if(nextPageToken!=null){
            request.setPageToken(nextPageToken);
        }
        PlaylistListResponse response = request.execute();


        List<Playlist> itemList = response.getItems();
        for(int i = 0; i<itemList.size();i++){
            Playlist playList = itemList.get(i);
            String id = playList.getId();
            String title = playList.getSnippet().getTitle();
            
            list.add(new String[]{id,title});
        }

        if(response.getNextPageToken()!=null){
            List<String[]> nextPage = getPlayList(accessToken, response.getNextPageToken());
            for(var e : nextPage){ list.add(e); }    
        }
        return list;
    }
    public boolean addMusicToPlayList(String accessToken,String playlistId, Music music)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        
        try{
            YouTube youtubeService = getService(accessToken);
        
            // Define the PlaylistItem object, which will be uploaded as the request body.
            PlaylistItem playlistItem = new PlaylistItem();
            
            // Add the snippet object property to the PlaylistItem object.
            PlaylistItemSnippet snippet = new PlaylistItemSnippet();
            snippet.setPlaylistId(playlistId);
    
            ResourceId resourceId = new ResourceId();
            resourceId.setVideoId(music.getId());
            resourceId.setKind("youtube#video");
            snippet.setResourceId(resourceId);
            playlistItem.setSnippet(snippet);
            // Define and execute the API request
            YouTube.PlaylistItems.Insert request = youtubeService.playlistItems()
                .insert("snippet", playlistItem);
            request.setFields("snippet(title)");
            PlaylistItem response = request.execute();
            System.out.println(response.getSnippet().getTitle()+"이 재생목록에 추가되었습니다.");
            if(musicRespository.existsById(music.getId())){
                musicRespository.updateRequestCount(music.getId());                
            }else{
                music.setRequestCount(1l);
                musicRespository.save(music);
            }

            MusicRequestHistory mrh = new MusicRequestHistory();
            Member member = ((MemberInfoDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMember();
            Optional<Long> tempMaxId = musicRequestHistoryRepository.findMaxId();
            if(tempMaxId.isPresent()){
                mrh.setId(tempMaxId.get()+1);
            }else{
                mrh.setId(1l);
            }
            mrh.setMemberId(member.getEmail());
            mrh.setMusicId(music.getId());
            mrh.setPlaylistId(playlistId);
            mrh.setRequestAt(new Date());
            
            musicRequestHistoryRepository.save(mrh);

            return true;    
        }catch(Exception e){
            System.out.println("재생목록에 추가 실패");
            e.printStackTrace();
            return false;
        }
    }
    public void findVideoId(String accessToken, String title)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService(accessToken);
        // Define and execute the API request
        YouTube.Search.List request = youtubeService.search()
            .list("snippet");
        SearchListResponse response = request.setMaxResults(1L)
            .setQ(title)
            .setType("video")
            .setVideoLicense("youtube")
            .setFields("items(id(videoId),snippet(thumbnails(default(url)),title))")
            .execute();
        System.out.println(response);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    public void deletePlayList(String accessToken,String playListId)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService(accessToken);
        // Define and execute the API request
        YouTube.Playlists.Delete request = youtubeService.playlists()
            .delete(playListId);
        request.execute();
    }
    public void updatePlayList(String accessToken,String playListId,String title)
        throws GeneralSecurityException, IOException, GoogleJsonResponseException {
        YouTube youtubeService = getService(accessToken);
        
        // Define the Playlist object, which will be uploaded as the request body.
        Playlist playlist = new Playlist();
        
        // Add the id string property to the Playlist object.
        playlist.setId(playListId);
        
        // Add the snippet object property to the Playlist object.
        PlaylistSnippet snippet = new PlaylistSnippet();
        snippet.setTitle(title);
        playlist.setSnippet(snippet);

        // Define and execute the API request
        YouTube.Playlists.Update request = youtubeService.playlists()
            .update("snippet", playlist);
        Playlist response = request.execute();
        System.out.println(response);
    }
    */
}