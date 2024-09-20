import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() {
    // simulate multiple servers
    coroutineScope {
        launch {
            embeddedServer(Netty, port = 8081, host = "localhost", module = Application::module)
                .start(wait = true)
        }
        launch {
            embeddedServer(Netty, port = 8082, host = "localhost", module = Application::module)
                .start(wait = true)
        }
        // dedicated server for WS only
        launch {
            embeddedServer(Netty, port = 8083, host = "localhost", module = Application::module)
                .start(wait = true)
        }
    }
}