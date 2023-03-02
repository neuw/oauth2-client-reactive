package in.neuw.oauth2.client.reactive.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/v2")
public interface PingClientV2 {

    @GetExchange("/ping")
    Mono<ObjectNode> getPong();

    @PostExchange("/ping/create")
    Mono<ObjectNode> createPong();

}
