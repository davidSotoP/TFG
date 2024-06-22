package exportador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import exportador.configuration.CustomAuditorAwareImpl;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class, HypermediaAutoConfiguration.class })
@OpenAPIDefinition(info = @Info(title = "Exportador", version = "v1"))
@SecurityScheme(name = "apiKey", type= SecuritySchemeType.APIKEY)
@SecurityScheme(name = "basicAuth", type= SecuritySchemeType.HTTP, scheme = "basic")
@PropertySource("file:/C:/Users/David/Documents/TFG/application.properties")
public class ExportadorApplication implements WebMvcConfigurer{

	
	public static void main(String[] args) {
		SpringApplication.run(ExportadorApplication.class, args);

	}
	
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
	@EnableScheduling
	@Configuration
	class WebSecurityConfig extends WebSecurityConfigurerAdapter {
		
		private final String[] authWhiteList = {
				"https://reacttfg-f587ac1e7f28.herokuapp.com/*",
				"/v3/api-docs/**", "/swagger-ui/**",
				"/", "/api-docs/**",
				"/actuator/**",
				"/v1/api/**"
		};
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable().authorizeRequests()
			.antMatchers(authWhiteList).permitAll()
			.anyRequest().authenticated();
			
		}
		
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("*")
				.exposedHeaders("Authorization").allowCredentials(true).maxAge(3600);
	}
	

	@Configuration
	@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
	class SpringDataConfig {
		
		@Bean
		AuditorAware<String> auditorProvider() {
			return new CustomAuditorAwareImpl();
		}
	}
}

