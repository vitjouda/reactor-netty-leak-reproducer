package com.reactornetty.leakreproducer.controller

import com.reactornetty.leakreproducer.model.Data
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.net.URI

@Component
class Reproducer(
    val serverAClient: WebClient,
    val serverBClient: WebClient
) : ApplicationListener<ApplicationReadyEvent> {

    val httpClient = HttpClient.create(ConnectionProvider.newConnection())

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        (1..10).forEach {
            // init long-lived Channels that capture re-usable channel
            startSocket()
        }
        Flux.fromIterable(1..10000000000000)
            .flatMap({
                // mix calls between different Channel pools within one Reactor reference chain
                serverAClient
                    .get()
                    .uri("/randomDisconnect")
                    .retrieve()
                    .bodyToMono(Data::class.java)
                    .flatMap {
                        serverBClient
                            .get()
                            .uri("/randomDisconnect")
                            .retrieve()
                            .bodyToMono(Data::class.java)
                    }
                    .flatMap {
                        serverAClient
                            .get()
                            .uri("/randomDisconnect")
                            .retrieve()
                            .bodyToMono(Data::class.java)
                    }
                    .onErrorResume { Mono.empty() }
            }, 10)
            .then()
            .doOnSubscribe {
                println("Started")
            }
            .doOnTerminate {
                println("Finished")
            }
            .doOnError { println(it) }
            .subscribe()
    }

    fun startSocket() {
        // just to capture the channel within WS channel reference chain
        serverAClient
            .get()
            .uri("/data")
            .retrieve()
            .bodyToMono(Data::class.java)
            .flatMap { result ->
                // result is ignored, I just need to capture shared Channel
                ReactorNettyWebSocketClient(httpClient).execute(URI("localhost:8083/ev")) {
                    it.receive()
                        .`as`(it::send)
                        .then()
                }
            }
            .repeat()
            .retry()
            .subscribe({}, {}, { println("WS Closed") })
    }
}