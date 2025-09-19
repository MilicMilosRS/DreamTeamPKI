package rs.tim33.PKI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(request -> {
			request.anyRequest().permitAll();
		}).csrf(t -> t.disable())
		.headers(t -> t.frameOptions(f -> f.disable()));


        return http.build();
    }
}
