package nebulosa.phd2.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import nebulosa.netty.NettyClient
import nebulosa.phd2.client.commands.CompletableCommand
import nebulosa.phd2.client.commands.PHD2Command
import java.util.*
import java.util.concurrent.CompletableFuture

class PHD2Client(
    override val host: String,
    override val port: Int = 4400,
) : NettyClient() {

    @JvmField internal val listeners = hashSetOf<PHD2EventListener>()
    @JvmField internal val commands = hashMapOf<String, CompletableCommand<*>>()

    override val channelInitialzer = object : ChannelInitializer<SocketChannel>() {

        override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(
                PHD2ProtocolDecoder(this@PHD2Client, OBJECT_MAPPER),
                PHD2ProtocolEncoder(OBJECT_MAPPER),
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
    fun <T> sendCommand(command: PHD2Command<T>): CompletableFuture<T> {
        val task = CompletableFuture<T>()
        val id = UUID.randomUUID().toString()
        val completableCommand = CompletableCommand(command, task, id)
        commands[id] = completableCommand
        channel.get()?.channel()?.writeAndFlush(completableCommand)
        return task
    }

    companion object {

        @JvmStatic private val OBJECT_MAPPER = ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}
