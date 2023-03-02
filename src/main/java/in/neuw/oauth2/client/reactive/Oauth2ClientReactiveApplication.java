package in.neuw.oauth2.client.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class, ReactiveUserDetailsServiceAutoConfiguration.class})
public class Oauth2ClientReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2ClientReactiveApplication.class, args);
    }

}
