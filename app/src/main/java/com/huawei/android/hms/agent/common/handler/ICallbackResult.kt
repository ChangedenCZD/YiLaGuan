package com.huawei.android.hms.agent.common.handler

/**
 * 回调接口
 */
interface ICallbackResult<R> {
    /**
     * 回调接口
     * @param rst 结果码
     * @param result 结果信息
     */
    fun onResult(rst: Int, result: R?)
}
