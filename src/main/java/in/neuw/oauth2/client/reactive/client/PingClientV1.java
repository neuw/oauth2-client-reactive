package in.neuw.oauth2.client.reactive.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/v1")
public interface PingClientV1 {

    @GetExchange("/ping")
    Mono<ObjectNode> getPong();

    @PostExchange("/ping/post")
    Mono<ObjectNode> postPong();

}
