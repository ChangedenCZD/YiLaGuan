package com.huawei.android.hms.agent.common

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.android.hms.agent.common.handler.CheckUpdateHandler
import com.huawei.hms.api.CheckUpdatelistener
import com.huawei.hms.api.HuaweiApiClient

/**
 * 应用自升级
 */
class CheckUpdateApi : BaseApiAgent(), CheckUpdatelistener {

    /**
     * 应用自升级回调接口
     */
    private var handler: CheckUpdateHandler? = null

    /**
     * 升级传入的activity
     */
    private var activity: Activity? = null

    /**
     * Huawei Api Client 连接回调
     * @param rst 结果码
     * @param client HuaweiApiClient 实例
     */
    override fun onConnect(rst: Int, client: HuaweiApiClient?) {

        HMSAgentLog.d("onConnect:$rst")

        val activityCur = ActivityMgr.INST.lastActivity

        if (activityCur != null && client != null) {
            client.checkUpdate(activityCur, this)
        } else if (activity != null && client != null) {
            client.checkUpdate(activity, this)
        } else {
            // 跟SE确认：activity 为 null ， 不处理 | Activity is null and does not need to be processed
            HMSAgentLog.e("no activity to checkUpdate")
            onCheckUpdateResult(HMSAgent.AgentResultCode.NO_ACTIVITY_FOR_USE)
            return
        }
    }

    override fun onResult(resultCode: Int) {
        onCheckUpdateResult(resultCode)
    }

    private fun onCheckUpdateResult(retCode: Int) {
        HMSAgentLog.i("checkUpdate:callback=" + StrUtils.objDesc(handler) + " retCode=" + retCode)
        if (handler != null) {
            Handler(Looper.getMainLooper()).post(CallbackCodeRunnable(handler, retCode))
            handler = null
        }

        activity = null
    }

    /**
     * 应用自升级接口
     * @param handler 应用自升级结果回调
     */
    fun checkUpdate(activity: Activity, handler: CheckUpdateHandler) {
        HMSAgentLog.i("checkUpdate:handler=" + StrUtils.objDesc(handler))
        this.handler = handler
        this.activity = activity
        connect()
    }
}
