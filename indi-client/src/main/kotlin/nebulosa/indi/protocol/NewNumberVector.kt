package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("newNumberVector")
class NewNumberVector : NewVector<OneNumber>(), MinMaxVector<OneNumber>
