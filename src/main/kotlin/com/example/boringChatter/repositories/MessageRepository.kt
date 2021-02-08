package com.example.boringChatter.repositories

import com.example.boringChatter.models.Message
import org.springframework.data.repository.CrudRepository

/**
 * Репозиторий Сообщений, реализован запрос выборки последних 20 сообщений по убыванию id
 */

interface MessageRepository : CrudRepository<Message, Long> {
    fun findTop20ByOrderByIdDesc(): List<Message>
}
