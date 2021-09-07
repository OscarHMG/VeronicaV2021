package com.rolandopalermo.facturacion.ec.app.config;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

import static com.rolandopalermo.facturacion.ec.app.common.Constants.URI_API_V1;
import static com.rolandopalermo.facturacion.ec.app.common.Constants.URI_OPERATIONS;
import static com.rolandopalermo.facturacion.ec.app.common.Constants.URI_PUBLIC;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:data.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("datasourceBean")
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        "select username as username, password as password, is_active as enabled from public.user where username=?")
                .authoritiesByUsernameQuery(
                        "select username as username, role as authority from public.user where username=?")
                .passwordEncoder(passwordEncoder);
    }
    
    
    
    
    
    

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/swagger-resources/**", "/webjars/**", URI_PUBLIC + "**").permitAll()
                .antMatchers("/swagger-ui.html").authenticated()
                .antMatchers(URI_OPERATIONS + "**", URI_API_V1 + "**").authenticated().and().cors().configurationSource(corsConfigurationSource())
                .and()
                .httpBasic()
                .realmName("Veronica");
    }
    
    // To enable CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        /*final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(ImmutableList.of("http://localhost")); // www - obligatory
        //configuration.setAllowedOrigins(ImmutableList.of("*"));  //set access from all domains
        configuration.setAllowedMethods(ImmutableList.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;*/
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setExposedHeaders(ImmutableList.of("*"));
        //configuration.addAllowedHeader("*");
        configuration.setAllowedOrigins(ImmutableList.of("http://localhost:80", "http://www.admin.duragasexpress.apptelink.com/"));
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowedMethods(ImmutableList.of("OPTIONS", "GET", "POST","PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("x-auth-token", "Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    
    /*@Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Cache-Control");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }*/
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
                @Override
                public void addCorsMappings(CorsRegistry registry) {
                        registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedOrigins("http://localhost:80, http://www.admin.duragasexpress.apptelink.com/")
                                .allowedHeaders("*");
                }
        };
    }
    
    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
    
    /*@Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("veronica").roles("ADMIN");
        auth.inMemoryAuthentication().withUser("user").password("veronica").roles("USER");
    }*/

    /*@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("veronica").roles("ADMIN");
        auth.inMemoryAuthentication().withUser("user").password("veronica").roles("USER");
    }*/
}