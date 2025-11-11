package com.team_5_back_repository.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Team5BackRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(Team5BackRepositoryApplication.class, args);
    }

}
