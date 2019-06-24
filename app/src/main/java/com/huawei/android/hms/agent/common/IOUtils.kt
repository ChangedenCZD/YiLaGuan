package com.huawei.android.hms.agent.common

import java.io.Closeable
import java.io.IOException

/**
 * 工具类
 */
object IOUtils {
    fun close(`object`: Closeable?) {
        if (`object` != null) {
            try {
                `object`.close()
            } catch (e: IOException) {
                HMSAgentLog.d("close fail")
            }

        }
    }
}
