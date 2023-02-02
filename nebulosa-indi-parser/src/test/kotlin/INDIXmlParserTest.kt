import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import nebulosa.indi.parser.INDIXmlParser
import nebulosa.indi.protocol.*
import java.io.ByteArrayInputStream

class INDIXmlParserTest : StringSpec() {

    init {
        "def blob vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "def switch vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "def number vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "def light vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "def text vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "new blob vector" {
            NewBLOBVector().also {
                it.device = "DEVICE"
                it.name = "NAME"
                it.state = PropertyState.OK
                it.timestamp = "2023-02-01T12:59:52"

                with(OneBLOB()) {
                    name = "NAME"
                    it.elements.add(this)
                }

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "new switch vector" {
            NewSwitchVector().also {
                it.device = "DEVICE"
                it.name = "NAME"
                it.state = PropertyState.OK
                it.timestamp = "2023-02-01T12:59:52"

                with(OneSwitch()) {
                    name = "NAME"
                    value = false
                    it.elements.add(this)
                }

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "new number vector" {
            NewNumberVector().also {
                it.device = "DEVICE"
                it.name = "NAME"
                it.state = PropertyState.OK
                it.timestamp = "2023-02-01T12:59:52"

                with(OneNumber()) {
                    name = "NAME"
                    value = 0.0
                    it.elements.add(this)
                }

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "new light vector" {
            NewLightVector().also {
                it.device = "DEVICE"
                it.name = "NAME"
                it.state = PropertyState.OK
                it.timestamp = "2023-02-01T12:59:52"

                with(OneLight()) {
                    name = "NAME"
                    value = PropertyState.OK
                    it.elements.add(this)
                }

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "new text vector" {
            NewTextVector().also {
                it.device = "DEVICE"
                it.name = "NAME"
                it.state = PropertyState.OK
                it.timestamp = "2023-02-01T12:59:52"

                with(OneText()) {
                    name = "NAME"
                    value = "VALUE"
                    it.elements.add(this)
                }

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "set blob vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "set switch vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "set number vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "set light vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
        "set text vector" {
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

                val parser = INDIXmlParser(it.toInputStream())
                (parser.readINDIProtocol() == it).shouldBeTrue()
            }
        }
    }

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        private inline fun INDIProtocol.toInputStream() = ByteArrayInputStream(toXML().encodeToByteArray())
    }
}
