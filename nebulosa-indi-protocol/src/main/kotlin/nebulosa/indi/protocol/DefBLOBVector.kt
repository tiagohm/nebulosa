package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("defBLOBVector")
class DefBLOBVector : DefVector<DefBLOB>(), BLOBVector<DefBLOB>
