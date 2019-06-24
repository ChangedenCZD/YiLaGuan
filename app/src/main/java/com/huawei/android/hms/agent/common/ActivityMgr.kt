package com.huawei.android.hms.agent.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * Activity Management Class
 * 此类注册了activity的生命周期监听，用来获取最新的activity给后续逻辑处理使用 | This class registers the life cycle monitoring of the activity to obtain the latest activity for subsequent logical processing using
 */
class ActivityMgr
/**
 * 私有构造方法 | Private construction methods
 * 防止外面直接创建实例 | Prevent external instances from being created directly
 */
private constructor() : Application.ActivityLifecycleCallbacks {

    /**
     * 应用程序 | application
     */
    private var application: Application? = null

    /**
     * 最新的activity列表，如果没有则为空列表 | Latest list of activity, if no, empty list
     */
    private val curActivities = ArrayList<Activity>()

    /**
     * activity onResume Event Monitoring
     */
    private val resumeCallbacks = ArrayList<IActivityResumeCallback>()

    /**
     * activity onPause Event Monitoring
     */
    private val pauseCallbacks = ArrayList<IActivityPauseCallback>()

    /**
     * activity onDestroyed Event Monitoring
     */
    private val destroyedCallbacks = ArrayList<IActivityDestroyedCallback>()

    /**
     * 获取最新的activity | Get the latest activity
     * @return 最新的activity | Latest activity
     */
    val lastActivity: Activity?
        get() = lastActivityInner

    /**
     * 获取最新的activity，如果没有则返回null | Gets the latest activity and returns null if not
     * @return 最新的activity | Latest activity
     */
    private val lastActivityInner: Activity?
        get() = synchronized(LOCK_LASTACTIVITIES) {
            return if (curActivities.size > 0) {
                curActivities[curActivities.size - 1]
            } else {
                null
            }
        }

    /**
     * 初始化方法 | Initialization method
     * @param app 应用程序 | application
     */
    fun init(app: Application, initActivity: Activity?) {
        HMSAgentLog.d("init")

        if (application != null) {
            application!!.unregisterActivityLifecycleCallbacks(this)
        }

        application = app
        setCurActivity(initActivity)
        app.registerActivityLifecycleCallbacks(this)
    }

    /**
     * 释放资源，一般不需要调用 | Frees resources, and generally does not need to call
     */
    fun release() {
        HMSAgentLog.d("release")
        if (application != null) {
            application!!.unregisterActivityLifecycleCallbacks(this)
        }

        clearCurActivities()
        clearActivitResumeCallbacks()
        clearActivitPauseCallbacks()
        application = null
    }

    /**
     * 注册activity onResume事件回调 | Registering an Activity Onresume event Callback
     * @param callback activity onResume事件回调 | Activity Onresume Event Callback
     */
    fun registerActivitResumeEvent(callback: IActivityResumeCallback) {
        HMSAgentLog.d("registerOnResume:" + StrUtils.objDesc(callback))
        resumeCallbacks.add(callback)
    }

    /**
     * 反注册activity onResume事件回调 | unregistration Activity Onresume Event Callback
     * @param callback 已经注册的 activity onResume事件回调 | Registered Activity Onresume Event callback
     */
    fun unRegisterActivitResumeEvent(callback: IActivityResumeCallback) {
        HMSAgentLog.d("unRegisterOnResume:" + StrUtils.objDesc(callback))
        resumeCallbacks.remove(callback)
    }

    /**
     * 注册activity onPause 事件回调 | Registering an Activity OnPause event Callback
     * @param callback activity onPause 事件回调 | Activity OnPause Event Callback
     */
    fun registerActivitPauseEvent(callback: IActivityPauseCallback) {
        HMSAgentLog.d("registerOnPause:" + StrUtils.objDesc(callback))
        pauseCallbacks.add(callback)
    }

    /**
     * 反注册activity onPause事件回调 | unregistration activity OnPause Event Callback
     * @param callback 已经注册的 activity onPause事件回调 | Registered Activity OnPause Event callback
     */
    fun unRegisterActivitPauseEvent(callback: IActivityPauseCallback) {
        HMSAgentLog.d("unRegisterOnPause:" + StrUtils.objDesc(callback))
        pauseCallbacks.remove(callback)
    }

    /**
     * 注册activity onDestroyed 事件回调 | Registering an Activity ondestroyed event Callback
     * @param callback activity onDestroyed 事件回调 | Activity Ondestroyed Event Callback
     */
    fun registerActivitDestroyedEvent(callback: IActivityDestroyedCallback) {
        HMSAgentLog.d("registerOnDestroyed:" + StrUtils.objDesc(callback))
        destroyedCallbacks.add(callback)
    }

    /**
     * 反注册activity onDestroyed 事件回调 | unregistration Activity ondestroyed Event Callback
     * @param callback 已经注册的 activity onDestroyed事件回调 | Registered Activity ondestroyed Event callback
     */
    fun unRegisterActivitDestroyedEvent(callback: IActivityDestroyedCallback) {
        HMSAgentLog.d("unRegisterOnDestroyed:" + StrUtils.objDesc(callback))
        destroyedCallbacks.remove(callback)
    }

    /**
     * 清空 activity onResume事件回调 | Clear Activity Onresume Event callback
     */
    fun clearActivitResumeCallbacks() {
        HMSAgentLog.d("clearOnResumeCallback")
        resumeCallbacks.clear()
    }

    /**
     * 清空 activity onPause 事件回调 | Clear Activity OnPause Event callback
     */
    fun clearActivitPauseCallbacks() {
        HMSAgentLog.d("clearOnPauseCallback")
        pauseCallbacks.clear()
    }

    /**
     * activity onCreate 监听回调 | Activity OnCreate Listener Callback
     * @param activity 发生onCreate事件的activity | Activity that occurs OnCreate events
     * @param savedInstanceState 缓存状态数据 | Cached state data
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        HMSAgentLog.d("onCreated:" + StrUtils.objDesc(activity))
        setCurActivity(activity)
    }

    /**
     * activity onStart 监听回调 | Activity OnStart Listener Callback
     * @param activity 发生onStart事件的activity | Activity that occurs OnStart events
     */
    override fun onActivityStarted(activity: Activity) {
        HMSAgentLog.d("onStarted:" + StrUtils.objDesc(activity))
        setCurActivity(activity)
    }

    /**
     * activity onResume 监听回调 | Activity Onresume Listener Callback
     * @param activity 发生onResume事件的activity | Activity that occurs Onresume events
     */
    override fun onActivityResumed(activity: Activity) {
        HMSAgentLog.d("onResumed:" + StrUtils.objDesc(activity))
        setCurActivity(activity)

        val tmdCallbacks = ArrayList(resumeCallbacks)
        for (callback in tmdCallbacks) {
            callback.onActivityResume(activity)
        }
    }

    /**
     * activity onPause 监听回调 | Activity OnPause Listener Callback
     * @param activity 发生onPause事件的activity | Activity that occurs OnPause events
     */
    override fun onActivityPaused(activity: Activity) {
        HMSAgentLog.d("onPaused:" + StrUtils.objDesc(activity))
        val tmdCallbacks = ArrayList(pauseCallbacks)
        for (callback in tmdCallbacks) {
            callback.onActivityPause(activity)
        }
    }

    /**
     * activity onStop 监听回调 | Activity OnStop Listener Callback
     * @param activity 发生onStop事件的activity | Activity that occurs OnStop events
     */
    override fun onActivityStopped(activity: Activity) {
        HMSAgentLog.d("onStopped:" + StrUtils.objDesc(activity))
    }

    /**
     * activity onSaveInstanceState 监听回调 | Activity Onsaveinstancestate Listener Callback
     * @param activity 发生 onSaveInstanceState 事件的activity | Activity that occurs onsaveinstancestate events
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    /**
     * activity onDestroyed 监听回调 | Activity Ondestroyed Listener Callback
     * @param activity 发生 onDestroyed 事件的activity | Activity that occurs ondestroyed events
     */
    override fun onActivityDestroyed(activity: Activity) {
        HMSAgentLog.d("onDestroyed:" + StrUtils.objDesc(activity))
        removeActivity(activity)

        // activity onDestroyed 事件回调 | Activity Ondestroyed Event Callback
        val tmdCallbacks = ArrayList(destroyedCallbacks)
        for (callback in tmdCallbacks) {
            callback.onActivityDestroyed(activity, lastActivityInner!!)
        }
    }

    /**
     * 移除当前activity | Remove Current Activity
     * @param curActivity 要移除的activity | Activity to remove
     */
    private fun removeActivity(curActivity: Activity) {
        synchronized(LOCK_LASTACTIVITIES) {
            curActivities.remove(curActivity)
        }
    }

    /**
     * 设置最新的activity | Set up the latest activity
     * @param curActivity 最新的activity | Latest activity
     */
    private fun setCurActivity(curActivity: Activity?) {
        synchronized(LOCK_LASTACTIVITIES) {
            val idxCurActivity = curActivities.indexOf(curActivity)
            when {
                idxCurActivity == -1 -> curActivities.add(curActivity!!)
                idxCurActivity < curActivities.size - 1 -> {
                    curActivities.remove(curActivity)
                    curActivities.add(curActivity!!)
                }
                else -> {}
            }
        }
    }

    /**
     * 清理activities | Clean activities
     */
    private fun clearCurActivities() {
        synchronized(LOCK_LASTACTIVITIES) {
            curActivities.clear()
        }
    }

    companion object {

        /**
         * 单实例 | Single Instance
         */
        val INST = ActivityMgr()

        private val LOCK_LASTACTIVITIES = Any()
    }
}