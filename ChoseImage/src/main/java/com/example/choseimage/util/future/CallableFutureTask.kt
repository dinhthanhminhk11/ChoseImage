package com.example.choseimage.util.future

import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

class CallableFutureTask<T>(callable: Callable<T>) : FutureTask<T>(callable) {

    private val executorService = Executors.newSingleThreadExecutor()
    private var callback: FutureCallback<T>? = null

    fun execute(callback: FutureCallback<T>? = null) {
        this.callback = callback
        if (!isDone) {
            executorService.submit(this)
        } else {
            this.callback?.onSuccess(get())
        }
    }

    override fun done() {
        super.done()
        try {
            callback?.onSuccess(get())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: CancellationException) {
            e.printStackTrace()
        }
    }
}


interface FutureCallback<T> {
    fun onSuccess(result: T)
}
