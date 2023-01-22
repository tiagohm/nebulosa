package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("setBLOBVector")
class SetBLOBVector : SetVector<OneBLOB>(), BLOBVector<OneBLOB>
