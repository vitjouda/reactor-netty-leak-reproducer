package com.reactornetty.leakreproducer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LeakReproducerApplication

fun main(args: Array<String>) {
	runApplication<LeakReproducerApplication>(*args)
}
