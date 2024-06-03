package nebulosa.common.exec

interface LineReadListener {

    fun onInputRead(line: String)

    fun onErrorRead(line: String)

    fun interface OnInput : LineReadListener {

        override fun onErrorRead(line: String) = Unit
    }

    fun interface OnError : LineReadListener {

        override fun onInputRead(line: String) = Unit
    }
}
