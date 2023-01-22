package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("defLightVector")
class DefLightVector : DefVector<DefLight>(), LightVector<DefLight>
