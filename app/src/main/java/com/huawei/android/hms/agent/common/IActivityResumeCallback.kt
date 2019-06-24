package com.huawei.android.hms.agent.common

import android.app.Activity

/**
 * Activity onResume 事件回调接口
 */
interface IActivityResumeCallback {

    /**
     * Activity onResume回调
     * @param activity 发生 onResume 事件的activity
     */
    fun onActivityResume(activity: Activity)
}
