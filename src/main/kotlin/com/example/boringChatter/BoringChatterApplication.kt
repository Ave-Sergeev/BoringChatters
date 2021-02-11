package com.example.boringChatter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BoringChatterApplication

fun main(args: Array<String>) {
	runApplication<BoringChatterApplication>(*args)
}