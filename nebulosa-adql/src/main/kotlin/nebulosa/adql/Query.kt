package nebulosa.adql

import adql.query.ADQLQuery

data class Query internal constructor(@JvmField internal val query: ADQLQuery) : CharSequence {

    private val queryText: String by lazy(query::toADQL)

    override val length
        get() = queryText.length

    override fun get(index: Int) = queryText[index]

    override fun subSequence(startIndex: Int, endIndex: Int) = queryText.subSequence(startIndex, endIndex)

    override fun toString() = queryText
}
