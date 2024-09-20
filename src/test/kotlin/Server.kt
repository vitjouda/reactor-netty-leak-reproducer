import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.delay
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Application.module() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    install(WebSockets)

    routing {

        get("/data") {
            call.respond(Data("value"))
        }

        get("/randomDisconnect") {
            if (Math.random() > 0.9) {
                (call as RoutingApplicationCall).getPrivateProperty<RoutingApplicationCall, NettyApplicationCall>("engineCall")?.context?.disconnect()
            } else {
                call.respond(Data("value"))
            }
        }

        get("/block") {
            delay(1000000000000000)
        }

        webSocket("/ev") {
            println("WS Connected")
            delay(100000000000)
        }
    }
}

data class Data(
    val value: String
)

inline fun <reified T : Any, R> T.getPrivateProperty(name: String): R? =
    T::class
        .memberProperties
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.get(this) as? R