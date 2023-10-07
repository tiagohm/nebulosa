package nebulosa.phd2.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.json.SimpleJsonModule
import nebulosa.json.converters.PathConverter
import nebulosa.log.loggerFor
import nebulosa.netty.NettyClient
import nebulosa.phd2.client.commands.CompletableCommand
import nebulosa.phd2.client.commands.PHD2Command
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max

class PHD2Client(
    override val host: String,
    override val port: Int = 4400,
) : NettyClient() {

    @JvmField internal val listeners = hashSetOf<PHD2EventListener>()
    @JvmField internal val commands = hashMapOf<String, CompletableCommand<*>>()

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                PHD2ProtocolDecoder(this@PHD2Client, JSON_MAPPER),
                PHD2ProtocolEncoder(JSON_MAPPER),
                PHD2ProtocolHandler(this@PHD2Client),
            )
        }
    }

    fun registerListener(listener: PHD2EventListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: PHD2EventListener) {
        listeners.remove(listener)
    }

    @Synchronized
    fun <T> sendCommand(command: PHD2Command<T>, timeout: Long = 30): CompletableFuture<T> {
        val task = CompletableFuture<T>()
        val id = UUID.randomUUID().toString()

        val completableCommand = CompletableCommand(command, task, id)
        commands[id] = completableCommand
        channel.get()?.channel()?.writeAndFlush(completableCommand)

        return task.orTimeout(max(1L, timeout), TimeUnit.SECONDS)
            .whenComplete { _, e ->
                if (e != null) LOG.error("Command error: $command", e)
                commands.remove(id)
            }
    }

    fun <T> sendCommandSync(command: PHD2Command<T>, timeout: Long = 30): T {
        return sendCommand(command, timeout).get()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<PHD2Client>()

        private val MODULE = SimpleJsonModule()

        init {
            MODULE.addDeserializer(PathConverter)
        }

        private val JSON_MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(MODULE)
            .build()
    }
}
