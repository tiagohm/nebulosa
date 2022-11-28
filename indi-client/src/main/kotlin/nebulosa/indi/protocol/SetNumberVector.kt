package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("setNumberVector")
class SetNumberVector : SetVector<OneNumber>(), NumberVector<OneNumber>
