package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("defTextVector")
class DefTextVector : DefVector<DefText>(), TextVector<DefText> {

    class Builder {

        private var device = ""
        private var name = ""
        private var group = ""
        private var label = ""
        private var perm = PropertyPermission.RW
        private var state = PropertyState.IDLE
        private var timeout = 0.0
        private var elements = arrayListOf<DefText>()

        fun device(device: String) = apply { this.device = device }

        fun name(name: String) = apply { this.name = name }

        fun group(group: String) = apply { this.group = group }

        fun label(label: String) = apply { this.label = label }

        fun perm(perm: PropertyPermission) = apply { this.perm = perm }

        fun state(state: PropertyState) = apply { this.state = state }

        fun timeout(timeout: Double) = apply { this.timeout = timeout }

        fun add(element: DefText) = apply { this.elements.add(element) }

        fun addAll(element: Iterable<DefText>) = apply { this.elements.addAll(element) }

        fun build() = DefTextVector().also {
            it.device = device
            it.name = name
            it.group = group
            it.label = label
            it.perm = perm
            it.state = state
            it.timeout = timeout
        }
    }
}
