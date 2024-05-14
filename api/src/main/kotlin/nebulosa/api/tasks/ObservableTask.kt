package nebulosa.api.tasks

import io.reactivex.rxjava3.core.ObservableSource

interface ObservableTask<T : Any> : Task, ObservableSource<T>
