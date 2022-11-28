package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("defNumberVector")
class DefNumberVector : DefVector<DefNumber>(), NumberVector<DefNumber>
