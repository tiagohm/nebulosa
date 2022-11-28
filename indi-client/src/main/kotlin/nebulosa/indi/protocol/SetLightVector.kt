package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("setLightVector")
class SetLightVector : SetVector<OneLight>(), LightVector<OneLight>
