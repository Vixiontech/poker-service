package it.gti.cims.pokerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EntityScan(basePackages = "it.gti.cims")
@EnableReactiveMongoRepositories(basePackages = "it.gti.cims")
@EnableJpaRepositories(basePackages = "it.gti.cims")
@SpringBootApplication(scanBasePackages = "it.gti.cims")
public class PokerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokerServiceApplication.class, args);
	}

}
