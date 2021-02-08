package com.example.boringChatter.websockets

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * Класс настройки WebSocket,
 * обрабатываем сообщения по WebSocket, возвращаемые брокером сообщений.
 */

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfigure : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        //Регистрируем /chat.connect, включая дополнительно SockJS как альтернативный вариант обмена сообщениями,
        //когда WebSocket не доступен.
        registry.addEndpoint("/chat.connect").withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        //Включение брокера сообщений, возвращая сообщения клиенту по направлению указанным префиксом
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }
}