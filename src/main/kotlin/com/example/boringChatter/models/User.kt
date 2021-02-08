package com.example.boringChatter.models

/**
 * Класс User, хранится в базе
 */

class User (
    var name: String = "uncertain"
) {
    override fun toString(): String {
        return name
    }
}
