package com.reactornetty.leakreproducer.client

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import kotlin.math.truncate

@Configuration
class Config {

    @Bean
    @Scope("prototype")
    fun webClientBuilder(customizerProvider: ObjectProvider<WebClientCustomizer>): WebClient.Builder {
        val builder = WebClient.builder()
        customizerProvider.orderedStream().forEach { customizer: WebClientCustomizer -> customizer.customize(builder) }
        return builder
    }

    @Bean
    fun serverAClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .baseUrl("http://localhost:8081")
            .clientConnector(ReactorClientHttpConnector(customHttpClient()))
            .build()
    }

    @Bean
    fun serverBClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .baseUrl("http://localhost:8082")
            .clientConnector(ReactorClientHttpConnector(customHttpClient()))
            .build()
    }

    protected fun customHttpClient(): HttpClient {
        return HttpClient.create(
            ConnectionProvider.builder("pool")
                .maxConnections(10)
                .pendingAcquireMaxCount(500)
                .pendingAcquireTimeout(Duration.ofHours(1))
                .build()
        )
    }
}