package in.neuw.oauth2.client.reactive.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import in.neuw.oauth2.client.reactive.client.PingClientV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class PingController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebClient pingClientJwt;

    @Autowired
    private WebClient pingClientOpaque;

    @Autowired
    private PingClientV1 pingClientV1;

    @Autowired
    private PingClientV1 pingClientV2;

    @GetMapping("/check/v1/ping")
    public Mono<ObjectNode> getPingJwt() {
        return pingClientV1.getPong();
    }

    @GetMapping("/check/v2/ping")
    public Mono<ObjectNode> getPingOpaque() {
        return pingClientOpaque.get().uri("/v2/ping").exchange()
                .flatMap(r -> r.bodyToMono(ObjectNode.class));
    }


    @PostMapping("/create/v1/ping")
    public Mono<ObjectNode> postPingJwt() {
        return pingClientV1.postPong();
    }

    @PostMapping("/create/v2/ping")
    public Mono<ObjectNode> postPingOpaque() {
        return pingClientOpaque.post().uri("/v2/ping/post").exchange()
                .flatMap(r -> r.bodyToMono(ObjectNode.class));
    }

}
