package Nomhala.Society;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SocietyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocietyApplication.class, args);
	}

}
