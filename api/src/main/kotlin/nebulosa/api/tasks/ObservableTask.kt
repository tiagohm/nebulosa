package nebulosa.api.tasks

import io.reactivex.rxjava3.core.ObservableSource
import java.util.function.Supplier

interface ObservableTask<T : Any> : Task, ObservableSource<T>, Supplier<T?>
