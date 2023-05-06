package nebulosa.desktop

import nebulosa.io.resourceUrl
import java.awt.*
import java.io.Closeable
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel

class SplashScreen : JFrame(), Closeable {

    init {
        setSize(640, 480)
        isUndecorated = true

        val icon = resourceUrl("icons/nebulosa.png")
        iconImage = ImageIO.read(icon)

        contentPane = object : JPanel(false) {

            override fun getPreferredSize() = Dimension(640, 480)

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g as Graphics2D)

                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                val logo = resourceUrl("icons/nebulosa-512.png")
                val image = ImageIO.read(logo)

                g.color = Color(0x25, 0x25, 0x25)
                g.fillRect(0, 0, width, height)

                g.drawImage(image, 192, 64, 256, 256, this@SplashScreen)

                g.color = Color.WHITE

                g.font = Font("Monospaced", Font.BOLD, 32)
                g.drawStringCentered("Nebulosa", 320, 388)

                g.font = Font("Monospaced", 0, 16)
                g.drawStringCentered("v${BuildConfig.VERSION_CODE} · ${BuildConfig.VERSION_NAME}", 320, 410)
            }
        }
    }

    fun open() {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }

    override fun close() {
        isVisible = false
    }

    companion object {

        @JvmStatic
        private fun Graphics.drawStringCentered(text: String, x: Int, y: Int) {
            val width = fontMetrics.stringWidth(text)
            drawString(text, x - width / 2, y)
        }
    }
}
