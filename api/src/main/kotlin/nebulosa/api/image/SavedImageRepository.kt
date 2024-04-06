package nebulosa.api.image

import io.objectbox.Box
import nebulosa.api.repositories.BoxRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SavedImageRepository(@Qualifier("savedImageBox") override val box: Box<SavedImageEntity>) : BoxRepository<SavedImageEntity>()
