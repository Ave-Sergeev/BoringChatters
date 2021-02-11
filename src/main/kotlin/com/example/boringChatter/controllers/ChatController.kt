package com.example.boringChatter.controllers

import com.example.boringChatter.models.Message
import com.example.boringChatter.models.User
import com.example.boringChatter.repositories.MessageRepository
import com.example.boringChatter.repositories.UserRepository
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.ArrayList

@Controller
class ChatController(val messageRepository: MessageRepository,
                        var userRepository: UserRepository?,
                        val messagingTemplate: SimpMessagingTemplate) {

    //Возвращаем список юзеров тому клиенту, который сделал запрос
    @MessageMapping("/users.list.req/{sessionId}")
    @SendTo("/topic/{sessionId}/userList")
    fun sendUsers(): List<String> {
        userRepository = UserRepository.instance
        val namesList: MutableList<String> = ArrayList()
        val usersList: ArrayList<User> = userRepository!!.allUsers

        usersList.forEach { user ->
            namesList.add(user.name)
        }

        return namesList
    }

    //Регистрируем пользователя и сообщаем об этом в ленте
    @MessageMapping("/chat.register")
    fun register(@Payload message: Message, headerAccessor: SimpMessageHeaderAccessor) {
        val sessionId = headerAccessor.sessionId
        val userName: String = message.author
        userRepository = UserRepository.instance
        userRepository?.addUser(sessionId!!, User(userName))

        //Сообщения в архиве
        val lastMessages: List<Message> = messageRepository.findTop20ByOrderByIdDesc()
        messagingTemplate.convertAndSend("/topic/lastMessages/$sessionId", lastMessages)
        message.sessionId = sessionId
        messageRepository.save(message)

        //Сообщяем о регистрации нового клиента
        messagingTemplate.convertAndSend("/topic/public", message)
    }

    //Отправляем всем пользователям принятое сообщение
    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    fun sendMessage(@Payload message: Message, headerAccessor: SimpMessageHeaderAccessor): Message {
        userRepository = UserRepository.instance
        val sessionId = headerAccessor.sessionId
        message.author = userRepository!!.getUserBySessionId(sessionId!!)!!.name
        message.sessionId = headerAccessor.sessionId
        messageRepository.save(message)

        return message
    }
}
