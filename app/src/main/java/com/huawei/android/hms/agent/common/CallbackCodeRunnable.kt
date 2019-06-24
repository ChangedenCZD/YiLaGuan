package com.huawei.android.hms.agent.common

import com.huawei.android.hms.agent.common.handler.ICallbackCode

/**
 * 回调线程
 */
class CallbackCodeRunnable(private val handlerInner: ICallbackCode?, private val rtnCodeInner: Int) : Runnable {

    override fun run() {
        handlerInner?.onResult(rtnCodeInner)
    }
}