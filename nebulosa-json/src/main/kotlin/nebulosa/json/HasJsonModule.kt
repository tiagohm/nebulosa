package nebulosa.json

import com.fasterxml.jackson.databind.module.SimpleModule

class HasJsonModule : SimpleModule() {

    init {
        addSerializer(HasJsonSerializer)
    }
}
