package com.example.endtoendencryptionsystem.handler

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.Function
import java.util.concurrent.TimeUnit

class ObservableRetryDelay(
        val retryConfigProvider: (Throwable) -> RetryConfig
) : Function<Observable<Throwable>, ObservableSource<*>> {

    private var retryCount: Int = 0

    override fun apply(throwableObs: Observable<Throwable>): ObservableSource<*> {
        return throwableObs
                .flatMap { error ->
                    val (maxRetries, delay, retryCondition) = retryConfigProvider(error)

                    if (++retryCount <= maxRetries) {
                        retryCondition()
                                .flatMapObservable { retry ->
                                    if (retry)
                                        Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                                    else
                                        Observable.error<Any>(error)
                                }
                    } else Observable.error<Any>(error)
                }
    }
}