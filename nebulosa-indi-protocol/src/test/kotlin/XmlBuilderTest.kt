import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import nebulosa.indi.protocol.xml.XmlBuilder

class XmlBuilderTest : StringSpec() {

    init {
        "only name" {
            XmlBuilder()
                .name("getProperties")
                .build() shouldBe "<getProperties/>"
        }
        "name and attribute" {
            XmlBuilder()
                .name("getProperties")
                .attr("version", "1.7")
                .build() shouldBe "<getProperties version=\"1.7\"/>"
        }
        "empty attribute" {
            XmlBuilder()
                .name("getProperties")
                .attr("version", "")
                .build() shouldBe "<getProperties/>"
        }
        "name and multiple attributes" {
            XmlBuilder()
                .name("getProperties")
                .attr("version", "1.7")
                .attr("device", "CCD Simulator")
                .build() shouldBe "<getProperties version=\"1.7\" device=\"CCD Simulator\"/>"
        }
        "with value" {
            XmlBuilder()
                .name("enableBLOB")
                .attr("device", "CCD Simulator")
                .value("Never")
                .build() shouldBe "<enableBLOB device=\"CCD Simulator\">Never</enableBLOB>"
        }
    }
}
