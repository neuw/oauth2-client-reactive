package in.neuw.oauth2.client.reactive.config;

import in.neuw.oauth2.client.reactive.client.PingClientV1;
import in.neuw.oauth2.client.reactive.client.PingClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class Oauth2ClientConfig {

    private Logger pingJWTLogger = LoggerFactory.getLogger("PING_JWT_CLIENT");
    private Logger pingOpaqueLogger = LoggerFactory.getLogger("PING_OPAQUE_CLIENT");

    @Value("${resource.server.basepath}")
    private String resourceServerBasePath;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                         final ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public WebClient pingClientJwt(ReactiveOAuth2AuthorizedClientManager authorizedClientManager, @Value("${zitadel.jwt.registration.name}") String registrationId) throws Exception {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        // for telling which oauth2 client registration to use for the webclient, being instantiated
        // if this line is skipped - we will get 401 from downstream, the client will not prepend the Authorization header with the downstream request
        oauth.setDefaultClientRegistrationId(registrationId);

        return WebClient.builder()
                // base path of the client, this way we need to set the complete url again
                .baseUrl(resourceServerBasePath)
                .filter(oauth)
                .filter(logRequest(pingJWTLogger))
                .filter(logResponse(pingJWTLogger))
                .build();
    }

    @Bean
    public WebClient pingClientOpaque(ReactiveOAuth2AuthorizedClientManager authorizedClientManager, @Value("${zitadel.opaque.registration.name}") String registrationId) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        // for telling which oauth2 client registration to use for the webclient, being instantiated
        // if this line is skipped - we will get 401 from downstream, the client will not prepend the Authorization header with the downstream request
        oauth.setDefaultClientRegistrationId(registrationId);

        return WebClient.builder()
                // base path of the client, this way we need to set the complete url again
                .baseUrl(resourceServerBasePath)
                .filter(oauth)
                .filter(logRequest(pingOpaqueLogger))
                .filter(logResponse(pingOpaqueLogger))
                .build();
    }

    @Bean
    PingClientV1 pingClientV1(WebClient pingClientJwt) {
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builder(WebClientAdapter.forClient(pingClientJwt))
                        .build();

        return httpServiceProxyFactory.createClient(PingClientV1.class);
    }

    @Bean
    PingClientV2 pingClientV2(WebClient pingClientJwt) {
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builder(WebClientAdapter.forClient(pingClientJwt))
                        .build();
        return httpServiceProxyFactory.createClient(PingClientV2.class);
    }

    private ExchangeFilterFunction logRequest(Logger logger) {
        return ExchangeFilterFunction.ofRequestProcessor(c -> {
            logger.info("Request: {} {}", c.method(), c.url());
            c.headers().forEach((n, v) -> {
                if (n.equalsIgnoreCase(AUTHORIZATION)) {
                    logger.info("request header {}={}", n, v);
                } else {
                    // as the AUTHORIZATION header is something security bounded
                    // will show up when the debug level logging is enabled
                    // for example using property - logging.level.root=DEBUG
                    logger.debug("request header {}={}", n, v);
                }
            });
            return Mono.just(c);
        });
    }

    private ExchangeFilterFunction logResponse(Logger logger) {
        return ExchangeFilterFunction.ofResponseProcessor(c -> {
            logger.info("Response: {}", c.statusCode());
            // if want to show the response headers in the log by any chance?
            /*c.headers().asHttpHeaders().forEach((n, v) -> {
                testWebClientLogger.info("response header {}={}", n, v);
            });*/
            return Mono.just(c);
        });
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf().disable().oauth2Client().and().build();
    }

}
