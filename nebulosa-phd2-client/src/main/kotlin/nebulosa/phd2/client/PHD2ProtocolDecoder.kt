package nebulosa.phd2.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.phd2.client.commands.CompletableCommand
import nebulosa.phd2.client.commands.PHD2CommandFailedException
import nebulosa.phd2.client.events.*

class PHD2ProtocolDecoder(
    private val client: PHD2Client,
    private val mapper: ObjectMapper,
) : ByteToMessageDecoder() {

    private val data = ByteArray(1024)
    @Volatile private var dataIdx = 0

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        for (i in 0 until buf.readableBytes()) {
            val b = buf.readByte()

            data[dataIdx++] = b

            if (b == 0x0A.toByte()) {
                try {
                    val eventTree = mapper.readTree(data, 0, dataIdx)

                    if (eventTree.has("jsonrpc")) {
                        processJsonRPC(eventTree)
                    } else {
                        processEvent(eventTree, out)
                    }
                } catch (e: Throwable) {
                    LOG.error("failed to process PHD2 message", e)
                } finally {
                    dataIdx = 0
                }

                return
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processJsonRPC(node: JsonNode) {
        LOG.debug { "$node" }

        val id = node.get("id").textValue()
        val command = client.commands.remove(id) as? CompletableCommand<Any>

        if (command == null) {
            LOG.error("command not found. id={}", id)
        } else if (node.has("error")) {
            val message = node.get("error").get("message").textValue()
            command.task.completeExceptionally(PHD2CommandFailedException(command.methodName, message))
        } else if (command.responseType != null) {
            val result = mapper.treeToValue(node.get("result"), command.responseType)
            command.task.complete(result)
        } else {
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

        @JvmStatic private val LOG = loggerFor<PHD2ProtocolDecoder>()

        @JvmStatic private val EVENT_TYPES = mapOf(
            "Alert" to (Alert::class.java to null),
            "AppState" to (AppState::class.java to null),
            "Calibrating" to (Calibrating::class.java to null),
            "CalibrationComplete" to (CalibrationComplete::class.java to null),
            "CalibrationDataFlipped" to (CalibrationDataFlipped::class.java to null),
            "CalibrationFailed" to (CalibrationFailed::class.java to null),
            "ConfigurationChange" to (ConfigurationChange::class.java to ConfigurationChange),
            "GuideParamChange" to (GuideParamChange::class.java to null),
            "GuideStep" to (GuideStep::class.java to null),
            "GuidingDithered" to (GuidingDithered::class.java to null),
            "GuidingStopped" to (GuidingStopped::class.java to GuidingStopped),
            "LockPositionLost" to (LockPositionLost::class.java to LockPositionLost),
            "LockPositionSet" to (LockPositionSet::class.java to null),
            "LockPositionShiftLimitReached" to (LockPositionShiftLimitReached::class.java to LockPositionShiftLimitReached),
            "LoopingExposures" to (LoopingExposures::class.java to null),
            "LoopingExposuresStopped" to (LoopingExposuresStopped::class.java to LoopingExposuresStopped),
            "Paused" to (Paused::class.java to Paused),
            "Resumed" to (Resumed::class.java to Resumed),
            "SettleBegin" to (SettleBegin::class.java to SettleBegin),
            "SettleDone" to (SettleDone::class.java to null),
            "Settling" to (Settling::class.java to null),
            "StarLost" to (StarLost::class.java to null),
            "StarSelected" to (StarSelected::class.java to null),
            "StartCalibration" to (StartCalibration::class.java to null),
            "StartGuiding" to (StartGuiding::class.java to StartGuiding),
            "Version" to (Version::class.java to null),
        )
    }
}
