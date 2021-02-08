package com.example.boringChatter.repositories

import com.example.boringChatter.models.User
import java.util.ArrayList
import java.util.HashMap

/**
 * Репозиторий Пользователя, реализовано:
 * получение пользователя по id, добавление пользователя,
 * удаление пользователя по id.
 */

class UserRepository {
    private val users: MutableMap<String, User>

    //Все методы synchronized, т.к. используют общий ресурс users: MutableMap<String, User>
    @get:Synchronized
    val allUsers: ArrayList<User>
        get() = ArrayList<User>(users.values)

    @Synchronized
    fun getUserBySessionId(sessionId: String): User? {
        return users[sessionId]
    }

    @Synchronized
    fun addUser(sessionId: String, user: User) {
        users[sessionId] = user
    }

    @Synchronized
    fun delUserBySessionId(sessionId: String) {
        users.remove(sessionId)
    }

    //Проверка дублирования одиночек
    companion object {
        @Volatile
        var instance: UserRepository? = null
            get() {
                var localInstance = field
                if (localInstance != null) return localInstance
                synchronized(UserRepository) {
                    localInstance = field
                    if (localInstance == null) {
                        localInstance = UserRepository()
                        field = localInstance
                    }
                }
                return localInstance
            }
        private set
    }

    init {
        users = HashMap<String, User>()
    }
}
