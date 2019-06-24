package com.huawei.android.hms.agent.hwid

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.android.hms.agent.common.*
import com.huawei.android.hms.agent.hwid.handler.SignInHandler
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.hwid.HuaweiId
import com.huawei.hms.support.api.hwid.HuaweiIdStatusCodes
import com.huawei.hms.support.api.hwid.SignInHuaweiId
import com.huawei.hms.support.api.hwid.SignInResult

/**
 * 帐号登录请求
 */
class SignInApi
/**
 * 私有构造方法
 */
private constructor() : BaseApiAgent() {

    /**
     * 登录结果回调
     */
    private var handler: SignInHandler? = null

    /**
     * 当前剩余重试次数
     */
    private var retryTimes = MAX_RETRY_TIMES

    /**
     * 待处理的signInResult
     */
    private var signInResultForDispose: SignInResult? = null

    /**
     * 获取待处理的signInResult，供 HMSSignInAgentActivity 调用
     * @return 待处理的signInResult
     */
    internal val signInResult: SignInResult?
        get() {
            HMSAgentLog.d("getSignInResult=" + StrUtils.objDesc(signInResultForDispose))
            return signInResultForDispose
        }

    /**
     * HuaweiApiClient 连接结果回调
     *
     * @param rst    结果码
     * @param client HuaweiApiClient 实例
     */
    override fun onConnect(rst: Int, client: HuaweiApiClient?) {
        if (client == null || !ApiClientMgr.INST.isConnect(client)) {
            HMSAgentLog.e("client not connted")
            onSignInResult(rst, null)
            return
        }

        val curActivity = ActivityMgr.INST.lastActivity
        if (curActivity == null) {
            HMSAgentLog.e("activity is null")
            onSignInResult(HMSAgent.AgentResultCode.NO_ACTIVITY_FOR_USE, null)
            return
        }

        val signInResult = HuaweiId.HuaweiIdApi.signIn(curActivity, client)
        signInResult.setResultCallback { result -> disposeSignInResult(result) }
    }

    /**
     * 处理signInResult回调
     * @param result 回调的signInResult实例
     */
    private fun disposeSignInResult(result: SignInResult?) {
        if (result == null) {
            HMSAgentLog.e("result is null")
            onSignInResult(HMSAgent.AgentResultCode.RESULT_IS_NULL, null)
            return
        }

        val status = result.status
        if (status == null) {
            HMSAgentLog.e("status is null")
            onSignInResult(HMSAgent.AgentResultCode.STATUS_IS_NULL, null)
            return
        }

        val rstCode = status.statusCode
        HMSAgentLog.d("status=$status")
        // 需要重试的错误码，并且可以重试
        if ((rstCode == CommonCode.ErrorCode.SESSION_INVALID || rstCode == CommonCode.ErrorCode.CLIENT_API_INVALID) && retryTimes > 0) {
            retryTimes--
            connect()
        } else {
            disposeObtainedResult(result, rstCode)
        }
    }

    /**
     * 处理回调的result
     * @param result 回调的result
     * @param rstCode 业务结果码
     */
    private fun disposeObtainedResult(result: SignInResult, rstCode: Int) {
        if (result.isSuccess) {
            //可以获取帐号的 openid，昵称，头像 at信息
            val account = result.signInHuaweiId
            onSignInResult(rstCode, account)
        } else {
            if (rstCode == HuaweiIdStatusCodes.SIGN_IN_UNLOGIN
                || rstCode == HuaweiIdStatusCodes.SIGN_IN_AUTH
                || rstCode == HuaweiIdStatusCodes.SIGN_IN_CHECK_PASSWORD
            ) {
                val curActivity = ActivityMgr.INST.lastActivity
                if (curActivity == null) {
                    HMSAgentLog.e("activity is null")
                    onSignInResult(HMSAgent.AgentResultCode.NO_ACTIVITY_FOR_USE, null)
                    return
                }

                try {
                    signInResultForDispose = result
                    val nxtIntent = Intent(curActivity, HMSSignInAgentActivity::class.java)
                    curActivity.startActivity(nxtIntent)
                } catch (e: Exception) {
                    HMSAgentLog.e("start HMSSignInAgentActivity error:" + e.message)
                    onSignInResult(HMSAgent.AgentResultCode.START_ACTIVITY_ERROR, null)
                }

            } else {
                onSignInResult(rstCode, null)
            }
        }
    }

    internal fun onSignInActivityResult(rstCode: Int, result: SignInHuaweiId?, needReSignIn: Boolean) {
        if (needReSignIn) {
            connect()
        } else {
            onSignInResult(rstCode, result)
        }
    }

    /**
     * 回调登录结果
     * @param rstCode 结果码
     * @param result 登录结果
     */
    private fun onSignInResult(rstCode: Int, result: SignInHuaweiId?) {
        HMSAgentLog.i("signIn:callback=" + StrUtils.objDesc(handler) + " retCode=" + rstCode)
        if (handler != null) {
            Handler(Looper.getMainLooper()).post(CallbackResultRunnable(handler, rstCode, result))
            handler = null
        }
        signInResultForDispose = null
        retryTimes = MAX_RETRY_TIMES
    }

    /**
     * 帐号登录请求
     * @param handler 登录结果回调
     */
    fun signIn(handler: SignInHandler) {
        HMSAgentLog.i("signIn:handler=" + StrUtils.objDesc(handler))

        if (this.handler != null) {
            HMSAgentLog.e("signIn:has already a signIn to dispose")
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

        val INST = SignInApi()

        /**
         * client 无效最大尝试次数
         */
        private val MAX_RETRY_TIMES = 1
    }
}
