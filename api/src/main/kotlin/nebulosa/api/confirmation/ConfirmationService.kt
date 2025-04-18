package nebulosa.api.confirmation

import nebulosa.api.message.MessageService
import nebulosa.log.d
import nebulosa.log.loggerFor

class ConfirmationService(private val messageService: MessageService) {

    private val confirmations = HashMap<String, ConfirmationLatch>()

    @Synchronized
    fun confirm(idempotencyKey: String, accepted: Boolean) {
        LOG.d { info("confirmed. idempotencyKey={}, accepted={}", idempotencyKey, accepted) }
        confirmations[idempotencyKey]?.confirm(accepted)
        confirmations.remove(idempotencyKey)
    }

    @Synchronized
    fun ask(idempotencyKey: String, event: ConfirmationEvent): ConfirmationLatch {
        confirmations[idempotencyKey]?.close()

        return ConfirmationLatch().also {
            confirmations[idempotencyKey] = it
            LOG.d { info("asking for confirmation. idempotencyKey={}, event={}", idempotencyKey, event::class.simpleName) }
            messageService.sendMessage(event)
        }
    }

    companion object {

        private val LOG = loggerFor<ConfirmationService>()
    }
}
