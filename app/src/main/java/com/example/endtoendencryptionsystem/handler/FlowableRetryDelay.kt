package com.example.endtoendencryptionsystem.handler


import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Function
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit

class FlowableRetryDelay(
        val retryConfigProvider: (Throwable) -> RetryConfig
) : Function<Flowable<Throwable>, Publisher<*>> {

    private var retryCount: Int = 0

    override fun apply(throwableFlowable: Flowable<Throwable>): Publisher<*> {
        return throwableFlowable
                .flatMap { error ->
                    val (maxRetries, delay, retryTransform) = retryConfigProvider(error)

                    if (++retryCount <= maxRetries) {
                        retryTransform()
                                .flatMapPublisher { retry ->
                                    if (retry)
                                        Flowable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                                    else
                                        Flowable.error<Any>(error)
                                }
                    } else Flowable.error<Any>(error)
                }
    }
}
