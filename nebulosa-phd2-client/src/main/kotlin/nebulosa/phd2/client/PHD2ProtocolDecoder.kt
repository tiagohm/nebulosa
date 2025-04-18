package nebulosa.phd2.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.phd2.client.commands.CompletableCommand
import nebulosa.phd2.client.commands.PHD2CommandFailedException
import nebulosa.phd2.client.events.AlertEvent
import nebulosa.phd2.client.events.AppStateEvent
import nebulosa.phd2.client.events.CalibratingEvent
import nebulosa.phd2.client.events.CalibrationCompleteEvent
import nebulosa.phd2.client.events.CalibrationDataFlippedEvent
import nebulosa.phd2.client.events.CalibrationFailedEvent
import nebulosa.phd2.client.events.ConfigurationChangeEvent
import nebulosa.phd2.client.events.GuideParamChangeEvent
import nebulosa.phd2.client.events.GuideStepEvent
import nebulosa.phd2.client.events.GuidingDitheredEvent
import nebulosa.phd2.client.events.GuidingStoppedEvent
import nebulosa.phd2.client.events.LockPositionLostEvent
import nebulosa.phd2.client.events.LockPositionSetEvent
import nebulosa.phd2.client.events.LockPositionShiftLimitReachedEvent
import nebulosa.phd2.client.events.LoopingExposuresEvent
import nebulosa.phd2.client.events.LoopingExposuresStoppedEvent
import nebulosa.phd2.client.events.PausedEvent
import nebulosa.phd2.client.events.ResumedEvent
import nebulosa.phd2.client.events.SettleBeginEvent
import nebulosa.phd2.client.events.SettleDoneEvent
import nebulosa.phd2.client.events.SettlingEvent
import nebulosa.phd2.client.events.StarLostEvent
import nebulosa.phd2.client.events.StarSelectedEvent
import nebulosa.phd2.client.events.StartCalibrationEvent
import nebulosa.phd2.client.events.StartGuidingEvent
import nebulosa.phd2.client.events.VersionEvent
import java.io.ByteArrayOutputStream

class PHD2ProtocolDecoder(
    private val client: PHD2Client,
    private val mapper: ObjectMapper,
) : ByteToMessageDecoder() {

    private val data = object : ByteArrayOutputStream(32 * 1024) {

        fun readTree() = mapper.readTree(buf, 0, count)
    }

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        for (i in 0 until buf.readableBytes()) {
            val b = buf.readByte()

            data.write(b.toInt())

            if (b == 0x0A.toByte()) {
                try {
                    val eventTree = data.readTree()

                    if (eventTree.has("jsonrpc")) {
                        processJsonRPC(eventTree)
                    } else {
                        processEvent(eventTree, out)
                    }
                } catch (e: Throwable) {
                    LOG.error("failed to process PHD2 message", e)
                } finally {
                    data.reset()
                }

                return
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processJsonRPC(node: JsonNode) {
        val id = node.get("id").textValue()
        val command = client.commands.remove(id) as? CompletableCommand<Any>

        if (command == null) {
            LOG.d { warn("command not found. id={}", id) }
        } else if (node.has("error")) {
            val message = node.get("error").get("message").textValue()
            client.listeners.forEach { it.onCommandProcessed(command.command, null, message) }
            command.task.completeExceptionally(PHD2CommandFailedException(command.methodName, message))
        } else if (command.responseType != null) {
            val result = mapper.treeToValue(node.get("result"), command.responseType)
            client.listeners.forEach { it.onCommandProcessed(command.command, result, null) }
            command.task.complete(result)
        } else {
            client.listeners.forEach { it.onCommandProcessed(command.command, null, null) }
            command.task.complete(null)
        }
    }

    private fun processEvent(node: JsonNode, out: MutableList<Any>): Boolean {
        val eventName = node.get("Event").textValue()
        val (type, value) = EVENT_TYPES[eventName] ?: return false

        return if (value != null) {
            out.add(value)
        } else {
            out.add(mapper.treeToValue(node, type))
        }
    }

    companion object {

        private val LOG = loggerFor<PHD2ProtocolDecoder>()

        private val EVENT_TYPES = mapOf(
            "Alert" to (AlertEvent::class.java to null),
            "AppState" to (AppStateEvent::class.java to null),
            "Calibrating" to (CalibratingEvent::class.java to null),
            "CalibrationComplete" to (CalibrationCompleteEvent::class.java to null),
            "CalibrationDataFlipped" to (CalibrationDataFlippedEvent::class.java to null),
            "CalibrationFailed" to (CalibrationFailedEvent::class.java to null),
            "ConfigurationChange" to (ConfigurationChangeEvent::class.java to ConfigurationChangeEvent),
            "GuideParamChange" to (GuideParamChangeEvent::class.java to null),
            "GuideStep" to (GuideStepEvent::class.java to null),
            "GuidingDithered" to (GuidingDitheredEvent::class.java to null),
            "GuidingStopped" to (GuidingStoppedEvent::class.java to GuidingStoppedEvent),
            "LockPositionLost" to (LockPositionLostEvent::class.java to LockPositionLostEvent),
            "LockPositionSet" to (LockPositionSetEvent::class.java to null),
            "LockPositionShiftLimitReached" to (LockPositionShiftLimitReachedEvent::class.java to LockPositionShiftLimitReachedEvent),
            "LoopingExposures" to (LoopingExposuresEvent::class.java to null),
            "LoopingExposuresStopped" to (LoopingExposuresStoppedEvent::class.java to LoopingExposuresStoppedEvent),
            "Paused" to (PausedEvent::class.java to PausedEvent),
            "Resumed" to (ResumedEvent::class.java to ResumedEvent),
            "SettleBegin" to (SettleBeginEvent::class.java to SettleBeginEvent),
            "SettleDone" to (SettleDoneEvent::class.java to null),
            "Settling" to (SettlingEvent::class.java to null),
            "StarLost" to (StarLostEvent::class.java to null),
            "StarSelected" to (StarSelectedEvent::class.java to null),
            "StartCalibration" to (StartCalibrationEvent::class.java to null),
            "StartGuiding" to (StartGuidingEvent::class.java to StartGuidingEvent),
            "Version" to (VersionEvent::class.java to null),
        )
    }
}
