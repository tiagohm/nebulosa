package nebulosa.horizons

data class NonUniqueObjectException(val recordItems: List<String>) : Exception("${recordItems.size} matches found")
