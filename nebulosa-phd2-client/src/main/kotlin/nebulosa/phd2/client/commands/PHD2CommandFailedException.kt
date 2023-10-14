package nebulosa.phd2.client.commands

class PHD2CommandFailedException(methodName: String, message: String) : Exception("[$methodName]: $message")
