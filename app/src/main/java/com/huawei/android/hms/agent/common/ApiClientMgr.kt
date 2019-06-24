package com.huawei.android.hms.agent.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.huawei.android.hms.agent.HMSAgent
import com.huawei.hms.activity.BridgeActivity
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.api.HuaweiApiClient
import com.huawei.hms.support.api.hwid.HuaweiId
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions
import java.util.*


/**
 * Huawei Api Client 管理类 | Huawei API Client Management class
 * 负责HuaweiApiClient的连接，异常处理等 | Responsible for huaweiapiclient connection, exception handling, etc.
 */
class ApiClientMgr
/**
 * 私有构造方法 | Private construction methods
 */
private constructor() : HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener,
    IActivityResumeCallback, IActivityPauseCallback, IActivityDestroyedCallback {

    /**
     * 上下文，用来处理连接失败 | Context to handle connection failures
     */
    private var context: Context? = null

    /**
     * 当前应用包名 | Current Application Package Name
     */
    private var curAppPackageName: String? = null

    /**
     * HuaweiApiClient 实例 | Huaweiapiclient instance
     */
    private var apiClient: HuaweiApiClient? = null

    /**
     * 是否允许解决connect错误（解决connect错误需要拉起activity）
     * Whether to allow the connect error to be resolved (resolve connect error need to pull activity)
     */
    private var allowResolveConnectError = false

    /**
     * 是否正在解决连接错误 | Whether a connection error is being resolved
     */
    private var isResolving: Boolean = false

    /**
     * HMSSDK 解决错误的activity | HMSSDK to solve the wrong activity
     */
    private var resolveActivity: BridgeActivity? = null

    /**
     * 是否存在其他activity覆盖在升级activity之上
     * Is there any other activity covering the escalation activity
     */
    private var hasOverActivity = false

    /**
     * 当前剩余尝试次数 | Current number of remaining attempts
     */
    private var curLeftResolveTimes = MAX_RESOLVE_TIMES

    /**
     * 连接回调 | Connection callback
     */
    private val connCallbacks = ArrayList<IClientConnectCallback>()

    /**
     * 注册的静态回调 | Registered Static callback
     */
    private val staticCallbacks = ArrayList<IClientConnectCallback>()

    /**
     * 超时handler用来处理client connect 超时
     * Timeout handler to handle client connect timeout
     */
    private val timeoutHandler = Handler(Handler.Callback { msg ->
        val hasConnCallbacks: Boolean
        synchronized(CALLBACK_LOCK) {
            hasConnCallbacks = !connCallbacks.isEmpty()
        }

        if (msg != null && msg.what == APICLIENT_TIMEOUT_HANDLE_MSG && hasConnCallbacks) {
            HMSAgentLog.d("connect time out")
            resetApiClient()
            onConnectEnd(HMSAgent.AgentResultCode.APICLIENT_TIMEOUT)
            return@Callback true
        } else if (msg != null && msg.what == APICLIENT_STARTACTIVITY_TIMEOUT_HANDLE_MSG && hasConnCallbacks) {
            HMSAgentLog.d("start activity time out")
            onConnectEnd(HMSAgent.AgentResultCode.APICLIENT_TIMEOUT)
            return@Callback true
        } else if (msg != null && msg.what == UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT_HANDLE_MSG && hasConnCallbacks) {
            HMSAgentLog.d(
                "Discarded update dispose:hasOverActivity=" + hasOverActivity + " resolveActivity=" + StrUtils.objDesc(
                    resolveActivity
                )
            )
            if (hasOverActivity && resolveActivity != null && !resolveActivity!!.isFinishing) {
                onResolveErrorRst(ConnectionResult.CANCELED)
            }
            return@Callback true
        }
        false
    })

    /**
     * 初始化
     * @param app 应用程序
     */
    fun init(app: Application) {

        HMSAgentLog.d("init")

        // 保存应用程序context
        context = app.applicationContext

        // 取得应用程序包名
        curAppPackageName = app.packageName

        // 注册activity onResume回调
        ActivityMgr.INST.unRegisterActivitResumeEvent(this)
        ActivityMgr.INST.registerActivitResumeEvent(this)

        // 注册activity onPause回调
        ActivityMgr.INST.unRegisterActivitPauseEvent(this)
        ActivityMgr.INST.registerActivitPauseEvent(this)

        // 注册activity onDestroyed 回调
        ActivityMgr.INST.unRegisterActivitDestroyedEvent(this)
        ActivityMgr.INST.registerActivitDestroyedEvent(this)
    }

    /**
     * 断开apiclient，一般不需要调用
     */
    fun release() {
        HMSAgentLog.d("release")

        isResolving = false
        resolveActivity = null
        hasOverActivity = false

        val client = getApiClient()
        client?.disconnect()

        synchronized(APICLIENT_LOCK) {
            apiClient = null
        }

        synchronized(STATIC_CALLBACK_LOCK) {
            staticCallbacks.clear()
        }

        synchronized(CALLBACK_LOCK) {
            connCallbacks.clear()
        }
    }

    /**
     * 获取当前的 HuaweiApiClient
     * @return HuaweiApiClient 实例
     */
    fun getApiClient(): HuaweiApiClient? {
        synchronized(APICLIENT_LOCK) {
            return if (apiClient != null) apiClient else resetApiClient()
        }
    }

    /**
     * 判断client是否已经连接
     * @param client 要检测的client
     * @return 是否已经连接
     */
    fun isConnect(client: HuaweiApiClient?): Boolean {
        return client != null && client.isConnected
    }

    /**
     * 注册apiclient连接事件
     * @param staticCallback 连接回调
     */
    fun registerClientConnect(staticCallback: IClientConnectCallback) {
        synchronized(STATIC_CALLBACK_LOCK) {
            staticCallbacks.add(staticCallback)
        }
    }

    /**
     * 反注册apiclient连接事件
     * @param staticCallback 连接回调
     */
    fun removeClientConnectCallback(staticCallback: IClientConnectCallback) {
        synchronized(STATIC_CALLBACK_LOCK) {
            staticCallbacks.remove(staticCallback)
        }
    }

    /**
     * 重新创建apiclient
     * 2种情况需要重新创建：1、首次 2、client的状态已经紊乱
     * @return 新创建的client
     */
    private fun resetApiClient(): HuaweiApiClient? {
        if (context == null) {
            HMSAgentLog.e("HMSAgent not init")
            return null
        }

        synchronized(APICLIENT_LOCK) {
            if (apiClient != null) {
                // 对于老的apiClient，1分钟后才丢弃，防止外面正在使用过程中这边disConnect了
                disConnectClientDelay(apiClient!!, 60000)
            }

            HMSAgentLog.d("reset client")

            // 这种重置client，极端情况可能会出现2个client都回调结果的情况。此时可能出现rstCode=0，但是client无效。
            // 因为业务调用封装中都进行了一次重试。所以不会有问题
            val signInOptions =
                HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN).requestAccessToken()
                    .requestOpenId().requestUnionId().build()
            apiClient = HuaweiApiClient.Builder(context!!)
                .addApi(HuaweiId.SIGN_IN_API, signInOptions)
                .addConnectionCallbacks(INST)
                .addOnConnectionFailedListener(INST)
                .build()
            return apiClient
        }
    }

    /**
     * 连接 HuaweiApiClient,
     * @param callback 连接结果回调，一定不能为null,在子线程进行回调
     * @param allowResolve 是否允许解决错误，解决错误时可能会拉起界面
     */
    fun connect(callback: IClientConnectCallback, allowResolve: Boolean) {

        if (context == null) {
            aSysnCallback(HMSAgent.AgentResultCode.HMSAGENT_NO_INIT, callback)
            return
        }

        val client = getApiClient()
        // client 有效，则直接回调
        if (client != null && client.isConnected) {
            HMSAgentLog.d("client is valid")
            aSysnCallback(HMSAgent.AgentResultCode.HMSAGENT_SUCCESS, callback)
            return
        } else {
            // client无效，将callback加入队列，并启动连接
            synchronized(CALLBACK_LOCK) {
                HMSAgentLog.d("client is invalid：size=" + connCallbacks.size)
                allowResolveConnectError = allowResolveConnectError || allowResolve
                if (connCallbacks.isEmpty()) {
                    connCallbacks.add(callback)

                    // 连接尝试最大次数
                    curLeftResolveTimes = MAX_RESOLVE_TIMES

                    startConnect()
                } else {
                    connCallbacks.add(callback)
                }
            }
        }
    }

    /**
     * 线程中进行Huawei Api Client 的连接
     */
    private fun startConnect() {

        // 触发一次连接将重试次数减1
        curLeftResolveTimes--

        HMSAgentLog.d("start thread to connect")
        ThreadUtil.INST.excute(Runnable {
            val client = getApiClient()

            if (client != null) {
                HMSAgentLog.d("connect")
                val curActivity = ActivityMgr.INST.lastActivity

                // 考虑到有cp后台需要调用接口，HMSSDK去掉了activity不能为空的判断。这里只是取当前activity，可能为空
                timeoutHandler.sendEmptyMessageDelayed(APICLIENT_TIMEOUT_HANDLE_MSG, APICLIENT_CONNECT_TIMEOUT.toLong())
                client.connect(curActivity)
            } else {
                HMSAgentLog.d("client is generate error")
                onConnectEnd(HMSAgent.AgentResultCode.RESULT_IS_NULL)
            }
        })
    }

    /**
     * Huawei Api Client 连接结束方法
     * @param rstCode client 连接结果码
     */
    private fun onConnectEnd(rstCode: Int) {
        HMSAgentLog.d("connect end:$rstCode")

        synchronized(CALLBACK_LOCK) {
            // 回调各个回调接口连接结束
            for (callback in connCallbacks) {
                aSysnCallback(rstCode, callback)
            }
            connCallbacks.clear()

            // 恢复默认不显示
            allowResolveConnectError = false
        }

        synchronized(STATIC_CALLBACK_LOCK) {
            // 回调各个回调接口连接结束
            for (callback in staticCallbacks) {
                aSysnCallback(rstCode, callback)
            }
            staticCallbacks.clear()
        }
    }

    /**
     * 起线程回调各个接口，避免其中一个回调者耗时长影响其他调用者
     * @param rstCode 结果码
     * @param callback 回调
     */
    private fun aSysnCallback(rstCode: Int, callback: IClientConnectCallback) {
        ThreadUtil.INST.excute(Runnable {
            val client = getApiClient()
            HMSAgentLog.d("callback connect: rst=$rstCode apiClient=$client")
            callback.onConnect(rstCode, client!!)
        })
    }

    /**
     * Activity onResume回调
     *
     * @param activity 发生 onResume 事件的activity
     */
    override fun onActivityResume(activity: Activity) {
        // 通知hmssdk activity onResume了
        val client = getApiClient()
        if (client != null) {
            HMSAgentLog.d("tell hmssdk: onResume")
            client.onResume(activity)
        }

        // 如果正在解决错误，则处理被覆盖的场景
        HMSAgentLog.d("is resolving:$isResolving")
        if (isResolving && PACKAGE_NAME_HIAPP != curAppPackageName) {
            if (activity is BridgeActivity) {
                resolveActivity = activity
                hasOverActivity = false
                HMSAgentLog.d("received bridgeActivity:" + StrUtils.objDesc(resolveActivity))
            } else if (resolveActivity != null && !resolveActivity!!.isFinishing) {
                hasOverActivity = true
                HMSAgentLog.d("received other Activity:" + StrUtils.objDesc(resolveActivity))
            }
            timeoutHandler.removeMessages(UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT_HANDLE_MSG)
            timeoutHandler.sendEmptyMessageDelayed(
                UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT_HANDLE_MSG,
                UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT.toLong()
            )
        }
    }

    /**
     * Activity onPause回调
     *
     * @param activity 发生 onPause 事件的activity
     */
    override fun onActivityPause(activity: Activity) {
        // 通知hmssdk，activity onPause了
        val client = getApiClient()
        client?.onPause(activity)
    }

    /**
     * Activity onPause回调
     *
     * @param activityDestroyed 发生 onDestroyed 事件的activity
     * @param activityNxt       下个要显示的activity
     */
    override fun onActivityDestroyed(activityDestroyed: Activity, activityNxt: Activity) {
        if (activityNxt == null) {
            // 所有activity销毁后，重置client，否则公告的标志位还在，下次弹不出来
            resetApiClient()
        }
    }

    /**
     * connect fail 解决结果回调， 由 HMSAgentActivity 在 onActivityResult 中调用
     * @param result 解决结果
     */
    internal fun onResolveErrorRst(result: Int) {
        HMSAgentLog.d("result=$result")
        isResolving = false
        resolveActivity = null
        hasOverActivity = false

        if (result == ConnectionResult.SUCCESS) {
            val client = getApiClient()
            if (client != null && !client.isConnecting && !client.isConnected && curLeftResolveTimes > 0) {
                startConnect()
                return
            }
        }

        onConnectEnd(result)
    }

    /**
     * HMSAgentActivity 拉起拉了（走了onCreate）
     */
    internal fun onActivityLunched() {
        HMSAgentLog.d("resolve onActivityLunched")
        // 拉起界面回调，移除拉起界面超时
        timeoutHandler.removeMessages(APICLIENT_STARTACTIVITY_TIMEOUT_HANDLE_MSG)
        isResolving = true
    }

    /**
     * Huawe Api Client 连接成功回到
     */
    override fun onConnected() {
        HMSAgentLog.d("connect success")
        timeoutHandler.removeMessages(APICLIENT_TIMEOUT_HANDLE_MSG)
        onConnectEnd(ConnectionResult.SUCCESS)
    }

    /**
     * 当client变成断开状态时会被调用。这有可能发生在远程服务出现问题时（例如：出现crash或资源问题导致服务被系统杀掉）。
     * 当被调用时，所有的请求都会被取消，任何listeners都不会被执行。需要 CP 开发代码尝试恢复连接（connect）。
     * 应用程序应该禁用需要服务的相关UI组件，等待[.onConnected] 回调后重新启用他们。<br></br>
     *
     * @param cause 断开的原因. 常量定义： CAUSE_*.
     */
    override fun onConnectionSuspended(cause: Int) {
        HMSAgentLog.d("connect suspended")
        connect(EmptyConnectCallback("onConnectionSuspended try end:"), true)
    }

    /**
     * 建立client到service的连接失败时调用
     *
     * @param result 连接结果，用于解决错误和知道什么类型的错误
     */
    override fun onConnectionFailed(result: ConnectionResult?) {
        timeoutHandler.removeMessages(APICLIENT_TIMEOUT_HANDLE_MSG)

        if (result == null) {
            HMSAgentLog.e("result is null")
            onConnectEnd(HMSAgent.AgentResultCode.RESULT_IS_NULL)
            return
        }

        val errCode = result.errorCode
        HMSAgentLog.d("errCode=$errCode allowResolve=$allowResolveConnectError")

        if (HuaweiApiAvailability.getInstance().isUserResolvableError(errCode) && allowResolveConnectError) {
            val activity = ActivityMgr.INST.lastActivity
            if (activity != null) {
                try {
                    timeoutHandler.sendEmptyMessageDelayed(
                        APICLIENT_STARTACTIVITY_TIMEOUT_HANDLE_MSG,
                        APICLIENT_STARTACTIVITY_TIMEOUT.toLong()
                    )
                    val intent = Intent(activity, HMSAgentActivity::class.java)
                    intent.putExtra(HMSAgentActivity.CONN_ERR_CODE_TAG, errCode)
                    intent.putExtra(BaseAgentActivity.EXTRA_IS_FULLSCREEN, UIUtils.isActivityFullscreen(activity))
                    activity.startActivity(intent)
                    return
                } catch (e: Exception) {
                    HMSAgentLog.e("start HMSAgentActivity exception:" + e.message)
                    timeoutHandler.removeMessages(APICLIENT_STARTACTIVITY_TIMEOUT_HANDLE_MSG)
                    onConnectEnd(HMSAgent.AgentResultCode.START_ACTIVITY_ERROR)
                    return
                }

            } else {
                // 当前没有界面处理不了错误
                HMSAgentLog.d("no activity")
                onConnectEnd(HMSAgent.AgentResultCode.NO_ACTIVITY_FOR_USE)
                return
            }
        } else {
            //其他错误码直接透传
        }

        onConnectEnd(errCode)
    }

    companion object {

        /**
         * 单实例 | Single Instance
         */
        val INST = ApiClientMgr()

        /**
         * 应用市场包名 | HiApp's package name
         */
        private val PACKAGE_NAME_HIAPP = "com.huawei.appmarket"

        /**
         * 回调锁，避免连接回调紊乱 | Callback lock to avoid connection callback disorder
         */
        private val CALLBACK_LOCK = Any()

        /**
         * 静态注册回调锁，避免注册和回调紊乱 | Static registration callback lock to avoid registration and callback disturbances
         */
        private val STATIC_CALLBACK_LOCK = Any()

        /**
         * client操作锁，避免连接使用紊乱 | Client operation lock, avoid connection use disorder
         */
        private val APICLIENT_LOCK = Any()

        /**
         * api client 连接超时 | API Client Connection Timeout
         */
        private val APICLIENT_CONNECT_TIMEOUT = 30000

        /**
         * 解决升级错误时activity onResume 稳定在3秒时间判断BridgeActivity上面有没有其他activity
         * To resolve an upgrade error, activity Onresume stable at 3 seconds to determine if there are any other activity on bridgeactivity.
         */
        private val UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT = 3000

        /**
         * api client 解决错误拉起界面超时 | API client Resolution error Pull interface timeout
         */
        private val APICLIENT_STARTACTIVITY_TIMEOUT = 3000

        /**
         * client 连接超时消息 | Client Connection Timeout Message
         */
        private val APICLIENT_TIMEOUT_HANDLE_MSG = 3

        /**
         * client 拉起activity超时消息 | Client starts Activity Timeout message
         */
        private val APICLIENT_STARTACTIVITY_TIMEOUT_HANDLE_MSG = 4

        /**
         * 解决升级错误时activity onResume 稳定在3秒时间判断BridgeActivity上面有没有其他activity
         * To resolve an upgrade error, activity Onresume stable at 3 seconds to determine if there are any other activity on bridgeactivity.
         */
        private val UPDATE_OVER_ACTIVITY_CHECK_TIMEOUT_HANDLE_MSG = 5

        /**
         * 最大尝试连接次数 | Maximum number of attempts to connect
         */
        private val MAX_RESOLVE_TIMES = 3

        private fun disConnectClientDelay(clientTmp: HuaweiApiClient, delay: Int) {
            Handler().postDelayed({ clientTmp.disconnect() }, delay.toLong())
        }
    }
}
