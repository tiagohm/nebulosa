package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("newBLOBVector")
class NewBLOBVector : NewVector<OneBLOB>(), BLOBVector<OneBLOB>
