package nebulosa.api.confirmation

import nebulosa.api.message.MessageService
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service

@Service
class ConfirmationService(private val messageService: MessageService) {

    private val confirmations = HashMap<String, ConfirmationLatch>()

    @Synchronized
    fun confirm(idempotencyKey: String, accepted: Boolean) {
        LOG.info("confirmed. idempotencyKey={}, accepted={}", idempotencyKey, accepted)
        confirmations[idempotencyKey]?.confirm(accepted)
        confirmations.remove(idempotencyKey)
    }

    @Synchronized
    fun ask(idempotencyKey: String, event: ConfirmationEvent): ConfirmationLatch {
        confirmations[idempotencyKey]?.close()

        return ConfirmationLatch().also {
            confirmations[idempotencyKey] = it
            LOG.info("asking for confirmation. idempotencyKey={}, event={}", idempotencyKey, event::class.simpleName)
            messageService.sendMessage(event)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ConfirmationService>()
    }
}
