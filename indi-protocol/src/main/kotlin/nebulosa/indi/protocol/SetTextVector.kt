package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("setTextVector")
class SetTextVector : SetVector<OneText>(), TextVector<OneText>
