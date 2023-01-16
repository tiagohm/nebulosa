package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("defTextVector")
class DefTextVector : DefVector<DefText>(), TextVector<DefText>
