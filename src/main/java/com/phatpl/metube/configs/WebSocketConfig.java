package com.phatpl.metube.configs;//package com.phatpl.metube.configs;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        /*
//        They follow the convention that destinations for messages to be carried on to all
//        subscribed clients via the pub-sub model should be prefixed with topic. On the other
//        hand, destinations for private messages are typically prefixed by queue.
//         */
//        config.enableSimpleBroker("/topic/", "/queue/");
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                        .setAllowedOrigins("*").
//                withSockJS();
//    }
//}


import com.phatpl.metube.configs.CountViewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameHandler(), "/game")
                        .setAllowedOrigins("*");
    }

    @Bean
    public CountViewHandler gameHandler() {
        return new CountViewHandler();
    }
}