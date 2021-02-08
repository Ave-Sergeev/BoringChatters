package com.example.boringChatter.models

import java.time.LocalDateTime
import javax.persistence.*

/**
 * Класс Message, хранится в базе
 */

@Entity
class Message (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(columnDefinition = "TEXT")
    var text: String = "",
    var author: String = "",
    var sessionId: String? = "",
    var type: MessageType? = null,
    private var time: LocalDateTime? = null
) {
    //Перечисление состояний(в чате, вышел, присоединился)
    enum class MessageType {
        CHAT, LEAVE, JOIN
    }

    override fun toString(): String {
        return "\n Message: \n id = $id \n text = " +
                """ $text author = $author type = ${type!!.name}                
                """.trimIndent()
    }

    init {
        time = LocalDateTime.now()
    }
}
