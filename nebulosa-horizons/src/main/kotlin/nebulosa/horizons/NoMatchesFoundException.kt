package nebulosa.horizons

data object NoMatchesFoundException : HorizonsException("no matches found") {

    private fun readResolve(): Any = NoMatchesFoundException
}
