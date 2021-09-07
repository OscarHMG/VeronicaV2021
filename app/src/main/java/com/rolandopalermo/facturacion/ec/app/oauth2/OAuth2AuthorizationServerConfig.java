package com.rolandopalermo.facturacion.ec.app.oauth2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("tokenStoreBean")
    private TokenStore tokenStore;

    @Value("${oauth2.client}")
    private String auth2Client;

    @Value("${oauth2.secret}")
    private String auth2Secret;

    @Value("${oauth2.grant}")
    private String auth2Grant;

    @Value("${oauth2.resurce.id}")
    private String auth2ResourceId;

    

    
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore);
        endpoints.authenticationManager(this.authenticationManager);
        endpoints.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest hsr, HttpServletResponse rs, Object o) throws Exception {
                rs.setHeader("Access-Control-Allow-Origin", "http://localhost:80, http://www.admin.duragasexpress.apptelink.com/");
                rs.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                rs.setHeader("Access-Control-Max-Age", "3600");
                rs.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
                
                //System.out.println(hsr.getAuthType());
                return true;
            }
        });
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(auth2Client)
                .secret(auth2Secret)
                .authorizedGrantTypes(auth2Grant.split(","))
                .scopes("read", "write")
                .resourceIds(auth2ResourceId)
                //.resourceIds("api")
                .accessTokenValiditySeconds(1200)
                .refreshTokenValiditySeconds(1200);
    }
    
    

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }

}