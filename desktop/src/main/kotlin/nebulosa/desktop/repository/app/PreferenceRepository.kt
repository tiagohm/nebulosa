package nebulosa.desktop.repository.app

import nebulosa.desktop.data.PreferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PreferenceRepository : JpaRepository<PreferenceEntity, String>
