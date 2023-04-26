package nebulosa.desktop.repository

import nebulosa.desktop.data.app.Preference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PreferenceRepository : JpaRepository<Preference, String>
