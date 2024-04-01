package nebulosa.fits

interface HierarchKeyFormatter {

    fun format(key: String): String

    fun extraSpaceRequired(key: String): Int

    val isCaseSensitive: Boolean

    companion object : HierarchKeyFormatter by StandardHierarchKeyFormatter.DEFAULT
}
