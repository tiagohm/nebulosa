package nebulosa.common.exec

interface LineReadListener {

    fun onInputRead(line: String) = Unit

    fun onErrorRead(line: String) = Unit
}
