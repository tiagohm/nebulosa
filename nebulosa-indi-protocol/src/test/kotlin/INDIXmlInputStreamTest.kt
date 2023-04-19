import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.parser.INDIXmlInputStream
import java.io.ByteArrayInputStream

class INDIXmlInputStreamTest : StringSpec() {

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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
            }
        }
        "new blob vector" {
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
        "new switch vector" {
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
        "new number vector" {
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
        "new light vector" {
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
        "new text vector" {
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
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

                val parser = INDIXmlInputStream(it.toInputStream())
                parser.readINDIProtocol().shouldNotBeNull() shouldBeEqualToComparingFields it
            }
        }
    }

    companion object {

        @Suppress("NOTHING_TO_INLINE")
        private inline fun INDIProtocol.toInputStream() = ByteArrayInputStream(toXML().encodeToByteArray())
    }
}
