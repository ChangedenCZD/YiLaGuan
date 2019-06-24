package com.huawei.android.hms.agent.common

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

/**
 * 基础activity，用来处理公共的透明参数
 */
open class BaseAgentActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestActivityTransparent()
    }

    /**
     * 启用透明的跳板Activity
     */
    private fun requestActivityTransparent() {
        try {
            val intent = intent
            if (intent != null && intent.getBooleanExtra(EXTRA_IS_FULLSCREEN, false)) {
                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val window = window
            window?.addFlags(0x04000000)
        } catch (e: Exception) {
            HMSAgentLog.w("requestActivityTransparent exception:" + e.message)
        }

    }

    companion object {

        val EXTRA_IS_FULLSCREEN = "should_be_fullscreen"
    }
}
