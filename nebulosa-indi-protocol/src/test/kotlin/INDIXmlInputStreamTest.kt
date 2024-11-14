import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.indi.protocol.DefBLOB
import nebulosa.indi.protocol.DefBLOBVector
import nebulosa.indi.protocol.DefLight
import nebulosa.indi.protocol.DefLightVector
import nebulosa.indi.protocol.DefNumber
import nebulosa.indi.protocol.DefNumberVector
import nebulosa.indi.protocol.DefSwitch
import nebulosa.indi.protocol.DefSwitchVector
import nebulosa.indi.protocol.DefText
import nebulosa.indi.protocol.DefTextVector
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.NewBLOBVector
import nebulosa.indi.protocol.NewLightVector
import nebulosa.indi.protocol.NewNumberVector
import nebulosa.indi.protocol.NewSwitchVector
import nebulosa.indi.protocol.NewTextVector
import nebulosa.indi.protocol.OneBLOB
import nebulosa.indi.protocol.OneLight
import nebulosa.indi.protocol.OneNumber
import nebulosa.indi.protocol.OneSwitch
import nebulosa.indi.protocol.OneText
import nebulosa.indi.protocol.PropertyPermission
import nebulosa.indi.protocol.PropertyState
import nebulosa.indi.protocol.SetBLOBVector
import nebulosa.indi.protocol.SetLightVector
import nebulosa.indi.protocol.SetNumberVector
import nebulosa.indi.protocol.SetSwitchVector
import nebulosa.indi.protocol.SetTextVector
import nebulosa.indi.protocol.parser.INDIXmlInputStream
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream

class INDIXmlInputStreamTest {

    @Test
    fun defBlobVector() {
        DefBLOBVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.label = "LABEL"
            it.group = "GROUP"
            it.state = PropertyState.OK
            it.perm = PropertyPermission.RW
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"

            with(DefBLOB()) {
                name = "NAME"
                label = "LABEL"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun defSwitchVector() {
        DefSwitchVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.label = "LABEL"
            it.group = "GROUP"
            it.state = PropertyState.OK
            it.perm = PropertyPermission.RW
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"

            with(DefSwitch()) {
                name = "NAME"
                label = "LABEL"
                value = false
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun defNumberVector() {
        DefNumberVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.label = "LABEL"
            it.group = "GROUP"
            it.state = PropertyState.OK
            it.perm = PropertyPermission.RW
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"

            with(DefNumber()) {
                name = "NAME"
                label = "LABEL"
                value = 0.0
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun defLightVector() {
        DefLightVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.label = "LABEL"
            it.group = "GROUP"
            it.state = PropertyState.OK
            it.timestamp = "2023-02-01T12:59:52"

            with(DefLight()) {
                name = "NAME"
                label = "LABEL"
                value = PropertyState.OK
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun defTextVector() {
        DefTextVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.label = "LABEL"
            it.group = "GROUP"
            it.state = PropertyState.OK
            it.perm = PropertyPermission.RW
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"

            with(DefText()) {
                name = "NAME"
                label = "LABEL"
                value = "VALUE"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun newBlobVector() {
        NewBLOBVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.timestamp = "2023-02-01T12:59:52"

            with(OneBLOB()) {
                name = "NAME"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun newSwitchVector() {
        NewSwitchVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.timestamp = "2023-02-01T12:59:52"

            with(OneSwitch()) {
                name = "NAME"
                value = false
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun newNumberVector() {
        NewNumberVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.timestamp = "2023-02-01T12:59:52"

            with(OneNumber()) {
                name = "NAME"
                value = 0.0
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun newLightVector() {
        NewLightVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.timestamp = "2023-02-01T12:59:52"

            with(OneLight()) {
                name = "NAME"
                value = PropertyState.OK
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun newTextVector() {
        NewTextVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.timestamp = "2023-02-01T12:59:52"

            with(OneText()) {
                name = "NAME"
                value = "VALUE"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun setBlobVector() {
        SetBLOBVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.state = PropertyState.OK
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"
            it.message = "MESSAGE"

            with(OneBLOB()) {
                name = "NAME"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun setSwitchVector() {
        SetSwitchVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.state = PropertyState.OK
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"
            it.message = "MESSAGE"

            with(OneSwitch()) {
                name = "NAME"
                value = false
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun setNumberVector() {
        SetNumberVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.state = PropertyState.OK
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"
            it.message = "MESSAGE"

            with(OneNumber()) {
                name = "NAME"
                value = 0.0
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun setLightVector() {
        SetLightVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.state = PropertyState.OK
            it.timestamp = "2023-02-01T12:59:52"
            it.message = "MESSAGE"

            with(OneLight()) {
                name = "NAME"
                value = PropertyState.OK
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    @Test
    fun setTextVector() {
        SetTextVector().also {
            it.device = "DEVICE"
            it.name = "NAME"
            it.state = PropertyState.OK
            it.timeout = 60.0
            it.timestamp = "2023-02-01T12:59:52"
            it.message = "MESSAGE"

            with(OneText()) {
                name = "NAME"
                value = "VALUE"
                it.elements.add(this)
            }

            val parser = INDIXmlInputStream(it.toInputStream())
            parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
        }
    }

    companion object {

        private fun INDIProtocol.toInputStream(): InputStream {
            val baos = ByteArrayOutputStream(256)
            writeTo(PrintStream(baos))
            return ByteArrayInputStream(baos.toByteArray())
        }
    }
}
