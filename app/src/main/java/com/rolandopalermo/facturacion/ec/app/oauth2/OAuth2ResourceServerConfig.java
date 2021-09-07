package com.rolandopalermo.facturacion.ec.app.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import static com.rolandopalermo.facturacion.ec.app.common.Constants.URI_API_V1;
import static com.rolandopalermo.facturacion.ec.app.common.Constants.URI_OPERATIONS;

@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    @Qualifier("tokenStoreBean")
    private TokenStore tokenStore;

    @Value("${oauth2.resurce.id}")
    private String auth2ResourceId;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(auth2ResourceId);
        //resources.resourceId("oauth2-resource");
        resources.tokenStore(tokenStore);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .requestMatchers().antMatchers(URI_OPERATIONS + "**", URI_API_V1 + "**")
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(URI_OPERATIONS + "**").hasRole("ADMIN")
                .antMatchers(URI_API_V1 + "**").hasRole("USER");
    }

}