package com.huawei.android.hms.agent.common

import android.app.Activity
import android.view.WindowManager

/**
 * 工具类
 */
object UIUtils {
    /**
     * 判断当前activity是否为全屏
     * @param activity 当前activity
     * @return 是否全屏
     */
    fun isActivityFullscreen(activity: Activity): Boolean {
        val attrs = activity.window.attributes
        return attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN
    }
}
