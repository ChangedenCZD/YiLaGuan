package com.huawei.android.hms.agent.common

import com.huawei.hms.api.HuaweiApiClient

/**
 * 连接client空回调
 */
class EmptyConnectCallback(private val msgPre: String) : IClientConnectCallback {

    /**
     * HuaweiApiClient 连接结果回调
     *
     * @param rst    结果码
     * @param client HuaweiApiClient 实例
     */
    override fun onConnect(rst: Int, client: HuaweiApiClient?) {
        HMSAgentLog.d(msgPre + rst)
    }
}