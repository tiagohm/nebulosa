package nebulosa.desktop.gui

const val CLOSE_CIRCLE_ICON = "\uDB80\uDD59"
const val CONNECTION_ICON = "\uDB85\uDE16"
const val SEND_ICON = "\uDB81\uDC8A"
const val CIRCLE_ICON = "\uDB81\uDF65"
const val PLAY_ICON = "\uDB81\uDC0A"
const val STOP_ICON = "\uDB81\uDCDB"

@Suppress("SameParameterValue")
private fun convert(text: String): String {
    val s = text.toInt(16)

    return if (s in 0x10000..0x10FFFF) {
        val hi = ((s - 0x10000) / 0x400) + 0xD800
        val lo = ((s - 0x10000) % 0x400) + 0xDC00
        String(charArrayOf(hi.toChar(), lo.toChar()))
    } else {
        s.toChar().toString()
    }
}

fun main() {
    val char = convert("F04DB")

    for (c in char) {
        print("\\u%04X".format(c.code))
    }
}
