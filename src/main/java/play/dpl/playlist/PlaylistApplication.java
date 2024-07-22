package play.dpl.playlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PlaylistApplication{

	public static void main(String[] args) {
		SpringApplication.run(PlaylistApplication.class, args);
	}

}
