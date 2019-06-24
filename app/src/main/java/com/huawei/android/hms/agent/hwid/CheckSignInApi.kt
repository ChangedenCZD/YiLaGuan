package com.huawei.android.hms.agent.hwid

import android.os.Handler
import android.os.Looper
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.android.hms.agent.common.*
import com.huawei.android.hms.agent.hwid.handler.SignInHandler
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.hwid.HuaweiId
import com.huawei.hms.support.api.hwid.SignInHuaweiId
import com.huawei.hms.support.api.hwid.SignInResult

/**
 * 帐号登录请求
 */
/**
 * 私有构造方法
 */
class CheckSignInApi : BaseApiAgent() {

    /**
     * 登录结果回调
     */
    private var handler: SignInHandler? = null

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
            onCheckSignInResult(rst, null)
            return
        }

        val curActivity = ActivityMgr.INST.lastActivity
        if (curActivity == null) {
            HMSAgentLog.e("activity is null")
            onCheckSignInResult(HMSAgent.AgentResultCode.NO_ACTIVITY_FOR_USE, null)
            return
        }

        val checkSignInResult = HuaweiId.HuaweiIdApi.signIn(curActivity, client)
        checkSignInResult.setResultCallback { result -> disposeCheckSignInResult(result) }
    }

    /**
     * 处理signInResult回调
     * @param result 回调的signInResult实例
     */
    private fun disposeCheckSignInResult(result: SignInResult?) {
        if (result == null) {
            HMSAgentLog.e("result is null")
            onCheckSignInResult(HMSAgent.AgentResultCode.RESULT_IS_NULL, null)
            return
        }

        val status = result.status
        if (status == null) {
            HMSAgentLog.e("status is null")
            onCheckSignInResult(HMSAgent.AgentResultCode.STATUS_IS_NULL, null)
            return
        }

        val rstCode = status.statusCode
        HMSAgentLog.d("status=$status")
        // 需要重试的错误码，并且可以重试
        if ((rstCode == CommonCode.ErrorCode.SESSION_INVALID || rstCode == CommonCode.ErrorCode.CLIENT_API_INVALID) && retryTimes > 0) {
            retryTimes--
            connect()
        } else {
            if (result.isSuccess) {
                //可以获取帐号的 openid，昵称，头像 at信息
                val account = result.signInHuaweiId
                onCheckSignInResult(rstCode, account)
            } else {
                onCheckSignInResult(rstCode, null)
            }
        }
    }

    /**
     * 回调登录结果
     * @param rstCode 结果码
     * @param result 登录结果
     */
    private fun onCheckSignInResult(rstCode: Int, result: SignInHuaweiId?) {
        HMSAgentLog.i("checkSignIn:callback=" + StrUtils.objDesc(handler) + " retCode=" + rstCode)
        if (handler != null) {
            Handler(Looper.getMainLooper()).post(CallbackResultRunnable(handler, rstCode, result))
            handler = null
        }
        retryTimes = MAX_RETRY_TIMES
    }

    /**
     * 帐号登录请求,非强制登录
     * @param handler 登录结果回调
     */
    fun checkSignIn(handler: SignInHandler) {
        HMSAgentLog.i("checkSignIn:handler=" + StrUtils.objDesc(handler))

        if (this.handler != null) {
            HMSAgentLog.e("has already a signIn to dispose")
            Handler(Looper.getMainLooper()).post(
                CallbackResultRunnable(
                    handler,
                    HMSAgent.AgentResultCode.REQUEST_REPEATED,
                    null
                )
            )
            return
        }

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
