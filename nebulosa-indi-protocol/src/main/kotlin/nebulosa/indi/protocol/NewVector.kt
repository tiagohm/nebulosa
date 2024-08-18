package nebulosa.indi.protocol

sealed interface NewVector<E : OneElement<*>> : INDIProtocol, Vector<E>
