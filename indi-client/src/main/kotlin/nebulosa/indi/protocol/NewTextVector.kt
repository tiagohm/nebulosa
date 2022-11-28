package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("newTextVector")
class NewTextVector : NewVector<OneText>(), TextVector<OneText>
