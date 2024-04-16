package play.dpl.playlist.Mananger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import play.dpl.playlist.Entity.Member;

public class CacheManager {
    private static final CacheManager instance = new CacheManager();
    private Cache<String, Member> cacheMember;

    private CacheManager() {
        cacheMember = Caffeine.newBuilder()
                .maximumSize(100)
                .build();
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public Cache<String, Member> getCacheMember() {
        return cacheMember;
    }
}
