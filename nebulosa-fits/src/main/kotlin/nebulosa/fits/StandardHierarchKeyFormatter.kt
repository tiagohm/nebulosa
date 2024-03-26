package nebulosa.fits

data class StandardHierarchKeyFormatter(override val isCaseSensitive: Boolean) : HierarchKeyFormatter {

    override fun format(key: String): String {
        // cfitsio specifies a required space before the '=', so let's play nice with it.
        return key.let { if (isCaseSensitive) it else it.uppercase() }
            .replace('.', ' ') + " "
    }

    override fun extraSpaceRequired(key: String) = 1

    companion object {

        @JvmStatic val CASE_SENSITIVE = StandardHierarchKeyFormatter(true)
        @JvmStatic val CASE_INSENSITIVE = StandardHierarchKeyFormatter(false)
        @JvmStatic val DEFAULT = CASE_INSENSITIVE
    }
}
