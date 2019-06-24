package com.huawei.android.hms.agent.hwid

import android.os.Handler
import android.os.Looper
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.android.hms.agent.common.*
import com.huawei.android.hms.agent.hwid.handler.SignOutHandler
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.client.ResultCallback
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.hwid.HuaweiId
import com.huawei.hms.support.api.hwid.SignOutResult

/**
 * 帐号登录请求
 */
class SignOutApi : BaseApiAgent() {

    /**
     * 登出结果回调
     */
    private var handler: SignOutHandler? = null

    /**
     * 当前剩余重试次数
     */
    private var retryTimes = MAX_RETRY_TIMES

    /**
     * HuaweiApiClient 连接结果回调
     *
     * @param rst    结果码
     * @param client HuaweiApiClient 实例
     */
    override fun onConnect(rst: Int, client: HuaweiApiClient?) {
        if (client == null || !ApiClientMgr.INST.isConnect(client)) {
            HMSAgentLog.e("client not connted")
            onSignOutResult(rst, null)
            return
        }

        val signOutResult = HuaweiId.HuaweiIdApi.signOut(client)
        signOutResult.setResultCallback(ResultCallback { result ->
            if (result == null) {
                HMSAgentLog.e("result is null")
                onSignOutResult(HMSAgent.AgentResultCode.RESULT_IS_NULL, null)
                return@ResultCallback
            }

            val status = result.status
            if (status == null) {
                HMSAgentLog.e("status is null")
                onSignOutResult(HMSAgent.AgentResultCode.STATUS_IS_NULL, null)
                return@ResultCallback
            }

            val rstCode = status.statusCode
            HMSAgentLog.d("status=$status")
            // 需要重试的错误码，并且可以重试
            if ((rstCode == CommonCode.ErrorCode.SESSION_INVALID || rstCode == CommonCode.ErrorCode.CLIENT_API_INVALID) && retryTimes > 0) {
                retryTimes--
                connect()
            } else {
                onSignOutResult(rstCode, result)
            }
        })
    }

    /**
     * 回调登出结果
     * @param rstCode 结果码
     * @param result 登出结果
     */
    internal fun onSignOutResult(rstCode: Int, result: SignOutResult?) {
        HMSAgentLog.i("signOut:callback=" + StrUtils.objDesc(handler) + " retCode=" + rstCode)
        if (handler != null) {
            Handler(Looper.getMainLooper()).post(CallbackResultRunnable(handler, rstCode, result))
            handler = null
        }
        retryTimes = MAX_RETRY_TIMES
    }

    /**
     * 帐号登出请求
     */
    fun signOut(handler: SignOutHandler) {
        HMSAgentLog.i("signOut:handler=" + StrUtils.objDesc(handler))
        this.handler = handler
        retryTimes = MAX_RETRY_TIMES
        connect()
    }

    companion object {

        /**
         * client 无效最大尝试次数
         */
        private val MAX_RETRY_TIMES = 1
    }
}
