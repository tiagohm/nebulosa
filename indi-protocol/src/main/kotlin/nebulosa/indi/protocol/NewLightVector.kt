package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("newLightVector")
class NewLightVector : NewVector<OneLight>(), LightVector<OneLight>
