package nebulosa.grpc.server

import com.google.protobuf.Empty
import kotlinx.coroutines.flow.flow
import nebulosa.grpc.Event
import nebulosa.grpc.EventSender
import nebulosa.grpc.GetProperties
import nebulosa.grpc.NebulosaGrpcKt
import nebulosa.grpc.NewProperty
import nebulosa.grpc.driver.Driver
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class NebulosaService : NebulosaGrpcKt.NebulosaCoroutineImplBase(), EventSender, Closeable {

    @Volatile private var running = true
    private val events = LinkedBlockingQueue<Event>(1000)
    private val drivers = ConcurrentHashMap<String, Driver>()

    fun registerDriver(driver: Driver) {
        require(drivers.containsKey(driver.name)) { "driver ${driver.name} is already registered" }
        drivers[driver.name] = driver
        driver.attach(this)
    }

    fun unregisterDriver(driver: Driver) {
        drivers.remove(driver.name)?.detach(this)
    }

    override fun sendEvent(event: Event) {
        events.offer(event)
    }

    override suspend fun ask(request: GetProperties): Empty {
        if (request.device.isEmpty()) {
            drivers.forEach { it.value.ask(request.name) }
        } else {
            drivers[request.device]?.ask(request.name)
        }

        return Empty.getDefaultInstance()
    }

    override suspend fun send(request: NewProperty): Empty {
        drivers[request.device]?.handleNewProperty(request)
        return Empty.getDefaultInstance()
    }

    override fun events(request: Empty) = flow {
        while (running) {
            val event = events.take()
            if (event === EMPTY_EVENT) break
            emit(event)
        }
    }

    override fun close() {
        running = false
        events.offer(EMPTY_EVENT)
    }

    companion object {

        @JvmStatic private val EMPTY_EVENT = Event.getDefaultInstance()
    }
}
