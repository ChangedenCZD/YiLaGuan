package com.huawei.android.hms.agent.common

import android.app.Activity

/**
 * Activity onDestroyed 事件回调接口
 */
interface IActivityDestroyedCallback {

    /**
     * Activity onPause回调
     * @param activityDestroyed 发生 onDestroyed 事件的activity
     * @param activityNxt 下个要显示的activity
     */
    fun onActivityDestroyed(activityDestroyed: Activity, activityNxt: Activity)
}
