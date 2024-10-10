package nebulosa.api.beans.modules

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import nebulosa.api.autofocus.AutoFocusEventChartSerializer
import nebulosa.api.autofocus.CurvePointSerializer
import nebulosa.api.beans.converters.time.DurationDeserializer
import nebulosa.api.beans.converters.time.DurationSerializer
import nebulosa.api.cameras.CameraDeserializer
import nebulosa.api.cameras.CameraSerializer
import nebulosa.api.dustcap.DustCapDeserializer
import nebulosa.api.dustcap.DustCapSerializer
import nebulosa.api.focusers.FocuserDeserializer
import nebulosa.api.focusers.FocuserSerializer
import nebulosa.api.guiding.GuideOutputDeserializer
import nebulosa.api.guiding.GuideOutputSerializer
import nebulosa.api.indi.INDIPropertySerializer
import nebulosa.api.indi.INDIPropertyVectorSerializer
import nebulosa.api.lightboxes.LightBoxDeserializer
import nebulosa.api.lightboxes.LightBoxSerializer
import nebulosa.api.mounts.MountDeserializer
import nebulosa.api.mounts.MountSerializer
import nebulosa.api.rotators.RotatorDeserializer
import nebulosa.api.rotators.RotatorSerializer
import nebulosa.api.stardetector.StarPointSerializer
import nebulosa.api.wheels.WheelDeserializer
import nebulosa.api.wheels.WheelSerializer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import java.time.Duration

class DeviceModule : SimpleModule() {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        val serializers = SimpleSerializers()
        serializers.addSerializer(CameraSerializer())
        serializers.addSerializer(MountSerializer())
        serializers.addSerializer(RotatorSerializer())
        serializers.addSerializer(FocuserSerializer())
        serializers.addSerializer(WheelSerializer())
        serializers.addSerializer(GuideOutputSerializer())
        serializers.addSerializer(LightBoxSerializer())
        serializers.addSerializer(DustCapSerializer())
        serializers.addSerializer(INDIPropertyVectorSerializer())
        serializers.addSerializer(INDIPropertySerializer())
        serializers.addSerializer(StarPointSerializer())
        serializers.addSerializer(AutoFocusEventChartSerializer())
        serializers.addSerializer(CurvePointSerializer())
        serializers.addSerializer(DurationSerializer())
        context.addSerializers(serializers)

        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(Camera::class.java, CameraDeserializer())
        deserializers.addDeserializer(Mount::class.java, MountDeserializer())
        deserializers.addDeserializer(Rotator::class.java, RotatorDeserializer())
        deserializers.addDeserializer(Focuser::class.java, FocuserDeserializer())
        deserializers.addDeserializer(FilterWheel::class.java, WheelDeserializer())
        deserializers.addDeserializer(GuideOutput::class.java, GuideOutputDeserializer())
        deserializers.addDeserializer(LightBox::class.java, LightBoxDeserializer())
        deserializers.addDeserializer(DustCap::class.java, DustCapDeserializer())
        deserializers.addDeserializer(Duration::class.java, DurationDeserializer())
        context.addDeserializers(deserializers)
    }
}
