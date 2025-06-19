package com.example.endtoendencryptionsystem.http

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.FlowableTransformer
import io.reactivex.rxjava3.core.MaybeTransformer
import io.reactivex.rxjava3.core.SingleTransformer
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * @author  Ruins
 */
object RxSchedulers {
    /**
     * 封装Rx线程，io线程=>主线程
     *
     * 使用方式：用 compose 操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.ioToMain)
     * @param <T>
     * @return FlowableTransformer
    </T> */
    fun <Any : kotlin.Any> ioToMain(): FlowableTransformer<Any, Any> {
        return FlowableTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 封装Rx线程，主线程=>io线程
     *
     * 使用方式：用compose操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.mainToIo)
     * @param <T>
     * @return FlowableTransformer
    </T> */
    fun <Any : kotlin.Any> mainToIo(): FlowableTransformer<Any, Any> {
        return FlowableTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
        }
    }

    /**
     * 封装Rx线程，io线程=>主线程
     *
     * 使用方式：用 compose 操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.maybeIoToMain)
     * @param <T>
     * @return FlowableTransformer
    </T> */
    fun <Any : kotlin.Any> maybeIoToMain(): MaybeTransformer<Any, Any> {
        return MaybeTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 封装Rx线程，io线程=>主线程
     *
     * 使用方式：用compose操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.maybeMainToIo)
     * @param <T>
     * @return FlowableTransformer
    </T> */
    fun <Any : kotlin.Any> maybeMainToIo(): MaybeTransformer<Any, Any> {
        return MaybeTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
        }
    }


    /**
     * 封装Rx线程，io线程=>主线程
     *
     * 使用方式：用 compose 操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.o_io_main)
     * @param <T>
     * @return SingleTransformer
    </T> */
    fun <Any : kotlin.Any> singleIoToMain(): SingleTransformer<Any, Any> {
        return SingleTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 封装Rx线程，主线程=>io线程
     *
     * 使用方式：用compose操作符调用该方法
     *
     * ApiFactory
     * .getApi()
     * .login(userName, passWord)
     * .compose(RxSchedulers.f_io_main)
     * @param <T>
     * @return SingleTransformer
    </T> */
    fun <Any : kotlin.Any> singleMainToIo(): SingleTransformer<Any, Any> {
        return SingleTransformer<Any, Any> { upstream ->
            upstream.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
        }
    }

}