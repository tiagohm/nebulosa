package nebulosa.grpc.server

import nebulosa.grpc.BarkRequest
import nebulosa.grpc.BarkResponse
import nebulosa.grpc.DogGrpcKt

internal object NebulosaService : DogGrpcKt.DogCoroutineImplBase() {

    override suspend fun bark(request: BarkRequest): BarkResponse {
        return BarkResponse.newBuilder().setMessage("Woof").build()
    }
}
