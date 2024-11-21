package nebulosa.horizons

data class NonUniqueObjectException(val recordItems: List<String>) : HorizonsException("${recordItems.size} matches found")
