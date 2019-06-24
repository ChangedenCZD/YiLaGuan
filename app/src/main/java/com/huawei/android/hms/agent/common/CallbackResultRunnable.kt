package com.huawei.android.hms.agent.common

import com.huawei.android.hms.agent.common.handler.ICallbackResult

/**
 * 回调线程
 */
class CallbackResultRunnable<R>(
    private val handlerInner: ICallbackResult<R>?,
    private val rtnCodeInner: Int,
    private val resultInner: R?
) : Runnable {

    override fun run() {
        handlerInner?.onResult(rtnCodeInner, resultInner)
    }
}