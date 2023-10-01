package nebulosa.api.sequencer

import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import org.springframework.batch.core.step.tasklet.StoppableTasklet

interface SequenceTasklet<T : Any> : StoppableTasklet, ObservableSource<T>, Observer<T> {

    fun subscribe(onNext: Consumer<in T>): Disposable
}
