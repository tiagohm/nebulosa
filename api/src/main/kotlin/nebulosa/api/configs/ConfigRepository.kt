package nebulosa.api.configs

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
interface ConfigRepository : JpaRepository<ConfigEntity, String> {

    fun text(key: String) = findByIdOrNull(key)?.value

    fun long(key: String) = text(key)?.toLongOrNull()

    fun save(key: String, value: Any?) = saveAndFlush(ConfigEntity(key, value?.toString()))
}
