package com.huawei.android.hms.agent.common

import android.os.Handler
import android.os.Looper

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 线程工具，用于执行线程等
 */
class ThreadUtil private constructor() {

    private var executors: ExecutorService? = null

    /**
     * 获取缓存线程池
     * @return 缓存线程池服务
     */
    private val executorService: ExecutorService?
        get() {
            if (executors == null) {
                try {
                    executors = Executors.newCachedThreadPool()
                } catch (e: Exception) {
                    HMSAgentLog.e("create thread service error:" + e.message)
                }

            }

            return executors
        }

    /**
     * 在线程中执行
     * @param runnable 要执行的runnable
     */
    fun excute(runnable: Runnable) {
        val executorService = executorService
        if (executorService != null) {
            // 优先使用线程池，提高效率
            executorService.execute(runnable)
        } else {
            // 线程池获取失败，则直接使用线程
            Thread(runnable).start()
        }
    }

    /**
     * 在主线程中执行
     * @param runnable 要执行的runnable
     */
    fun excuteInMainThread(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }

    companion object {
        val INST = ThreadUtil()
    }
}
