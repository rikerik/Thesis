package Erik.OnlineSong.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import Erik.OnlineSong.Filter.JwtAuthenticationFilter;
import Erik.OnlineSong.Service.UserDetailsServiceImp;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImp userDetailsServiceImp;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp, JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomLogoutHandler logoutHandler) {
        this.userDetailsServiceImp = userDetailsServiceImp;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /*
         * Disabling CSRF protection because the app uses jwt which is not susceptible
         * to CSRF attacks
         * Allows unauthenticated access to login and register endpoints
         * Specifying which userDetailsService to use
         * Configuring the session management to be stateless, because jwt is used for
         * authentication
         * No http session is created
         */
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req -> req.requestMatchers("/login/**", "/register/**", "/ws/**") // Allow access to WebSocket
                                .permitAll()
                                .requestMatchers("/admin_only/**").hasAnyAuthority("ADMIN") // Only allow access to
                                                                                            // admin users
                                .anyRequest() // All other requests require authentication
                                .authenticated())
                .userDetailsService(userDetailsServiceImp) // Specify the user details service
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Set session management to stateless
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add the JWT
                                                                                                      // filter before
                                                                                                      // the default
                                                                                                      // username/password
                                                                                                      // authentication
                                                                                                      // filter
                .logout(l -> l.logoutUrl("/logout")
                        .addLogoutHandler(logoutHandler) // Add the custom logout handler
                        .logoutSuccessHandler(
                                (request, response, authentication) -> SecurityContextHolder.clearContext())) // Clear
                                                                                                              // the
                                                                                                              // security
                                                                                                              // context
                                                                                                              // on
                                                                                                              // successful
                                                                                                              // logout
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Password encoder bean using BCrypt
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager(); // Authentication manager bean
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow requests from the specified origin
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000") // Allow requests from the frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specify allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow credentials in CORS requests
            }
        };
    }
}
