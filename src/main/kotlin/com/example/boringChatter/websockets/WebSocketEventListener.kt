package com.example.boringChatter.websockets

import com.example.boringChatter.models.Message
import com.example.boringChatter.repositories.MessageRepository
import com.example.boringChatter.repositories.UserRepository
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent

/**
 * Класс WebSocketEventListener, используется прослушивания событий сокетов,
 */

@Component
class WebSocketEventListener(val messagingTemplate: SimpMessagingTemplate,
                             val messageRepository: MessageRepository,
                             var userRepository: UserRepository?) {
    //Слушаем отключения
    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = headerAccessor.sessionId
        userRepository = UserRepository.instance
        val name: String = userRepository?.getUserBySessionId(sessionId!!)!!.name

        if (sessionId != null) {
            userRepository?.delUserBySessionId(sessionId)
        }

        val message = Message()
        message.author = name
        message.type = Message.MessageType.LEAVE

        if (sessionId != null) {
            message.sessionId = sessionId
        }

        messagingTemplate.convertAndSend("/topic/public", message)
        messageRepository.save(message)
    }
}
