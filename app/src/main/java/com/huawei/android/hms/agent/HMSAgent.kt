package com.huawei.android.hms.agent


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.huawei.android.hms.agent.common.*
import com.huawei.android.hms.agent.common.handler.CheckUpdateHandler
import com.huawei.android.hms.agent.common.handler.ConnectHandler
import com.huawei.android.hms.agent.hwid.CheckSignInApi
import com.huawei.android.hms.agent.hwid.SignInApi
import com.huawei.android.hms.agent.hwid.SignOutApi
import com.huawei.android.hms.agent.hwid.handler.SignInHandler
import com.huawei.android.hms.agent.hwid.handler.SignOutHandler
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.api.HuaweiApiClient

/**
 * HMSAgent 封装入口类。 提供了HMS SDK 功能的封装，使开发者更聚焦业务的处理。
 * HMSAgent encapsulates the entry class. Provides a encapsulation of the HMS SDK functionality that enables developers to focus more on business processing.
 */
class HMSAgent private constructor() : INoProguard {

    object AgentResultCode {

        /**
         * HMSAgent 成功 | success
         */
        val HMSAGENT_SUCCESS = 0

        /**
         * HMSAgent 没有初始化 | Hmsagent not initialized
         */
        val HMSAGENT_NO_INIT = -1000

        /**
         * 请求需要activity，但当前没有可用的activity | Request requires activity, but no active activity is currently available
         */
        val NO_ACTIVITY_FOR_USE = -1001

        /**
         * 结果为空 | Result is empty
         */
        val RESULT_IS_NULL = -1002

        /**
         * 状态为空 | Status is empty
         */
        val STATUS_IS_NULL = -1003

        /**
         * 拉起activity异常，需要检查activity有没有在manifest中配置 | Pull up an activity exception and need to check if the activity is configured in manifest
         */
        val START_ACTIVITY_ERROR = -1004

        /**
         * onActivityResult 回调结果错误 | Onactivityresult Callback Result Error
         */
        val ON_ACTIVITY_RESULT_ERROR = -1005

        /**
         * 重复请求 | Duplicate Request
         */
        val REQUEST_REPEATED = -1006

        /**
         * 连接client 超时 | Connect Client Timeout
         */
        val APICLIENT_TIMEOUT = -1007

        /**
         * 调用接口异常 | Calling an interface exception
         */
        val CALL_EXCEPTION = -1008

        /**
         * 接口参数为空 | Interface parameter is empty
         */
        val EMPTY_PARAM = -1009
    }


    /**
     * 帐号接口封装 | Account Interface Encapsulation
     */
    object Hwid {
        /**
         * 帐号登录请求 | Account Login Request
         * 当forceLogin为false时，如果当前没有登录授权，则直接回调错误码。| When Forcelogin is false, the error code is directly invoked if there is currently no login authorization.
         * 当forceLogin为true时，如果当前没有登录授权，则会拉起相应界面引导用户登录授权。 | When Forcelogin is true, if there is currently no login authorization, the corresponding interface is pulled to boot the user to logon authorization.
         * @param forceLogin 是否强制登录。 | Whether to force a login.
         * @param handler 登录结果回调（结果会在主线程回调） | Login result Callback (result is in main thread callback)
         */
        fun signIn(forceLogin: Boolean, handler: SignInHandler) {
            if (forceLogin) {
                SignInApi.INST.signIn(handler)
            } else {
                CheckSignInApi().checkSignIn(handler)
            }
        }

        /**
         * 帐号登出请求。此接口调用后，下次再调用signIn会拉起界面，请谨慎调用。如果不确定就不要调用了。 | Account Login Request. After this method is called, the next time you call signIn will pull the interface, please call carefully. Do not call if you are unsure.
         * @param handler 登出结果回调（结果会在主线程回调） | Logout result callback (result will be callback in main thread)
         */
        fun signOut(handler: SignOutHandler) {
            SignOutApi().signOut(handler)
        }
    }

    companion object {

        /**
         * 基础版本 | Base version
         */
        private val VER_020503001 = "020503001"

        /**
         * 2.6.0 版本1                                            | 2.6.0 version 1
         * 对外：接口不变                                         | External: interface unchanged
         * 对内：HMSSDK connect 接口增加activity参数              | Internal: HMSSDK connect interface to increase activity parameters
         * HMSSDK sign 接口增加activity参数                      | HMSSDK sign interface to increase activity parameters
         * 自身优化：                                             | Self optimization:
         * 1、增加了升级时被其他界面覆盖的处理                  | Increased handling of other interface coverage issues when upgrading
         * 2、game模块savePlayerInfo接口，去掉activity的判断    | Game Module Saveplayerinfo method to remove activity judgments
         * 3、解决错误回调成功，增加重试次数3次                 | Resolve error callback succeeded, increase retry count 3 times
         * 4、提供了多种HMSAgent初始化方法                      | Provides a variety of hmsagent initialization methods
         * 5、初始化时增加了版本号校验                          | Increased version number checksum during initialization
         */
        private val VER_020600001 = "020600001"

        /**
         * 2.6.0.200                                         | 2.6.0.200
         * 自身优化：                                        | Self optimization:
         * 1、增加shell脚本用来抽取代码和编译成jar            | Add shell script to extract code and compile into jar
         * 2、示例中manifest里面升级配置错误修复              | Example manifest upgrade configuration error Repair
         * 3、抽取代码中去掉manifest文件，只留纯代码          | Remove manifest files in the extraction code, leaving only pure code
         */
        private val VER_020600200 = "020600200"

        private val VER_020601002 = "020601002"

        private val VER_020601302 = "020601302"

        private val VER_020603306 = "020603306"

        /**
         * 当前版本号 | Current version number
         */
        val CURVER = VER_020603306

        private fun checkSDKVersion(context: Context): Boolean {
            val sdkMainVerL = (HuaweiApiAvailability.HMS_SDK_VERSION_CODE / 1000).toLong()
            val agentMainVerL = java.lang.Long.parseLong(CURVER) / 1000
            if (sdkMainVerL != agentMainVerL) {
                val errMsg =
                    "error: HMSAgent major version code ($agentMainVerL) does not match HMSSDK major version code ($sdkMainVerL)"
                HMSAgentLog.e(errMsg)
                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                return false
            }
            return true
        }

        /**
         * 初始化方法，传入第一个界面的activity   | Initialization method, passing in the first interface activity
         * @param activity 当前界面             | Current interface
         * @return true：成功 false：失败        | True: Success false: Failed
         */
        fun init(activity: Activity): Boolean {
            return init(null, activity)
        }

        /**
         * 初始化方法，建议在Application onCreate里面调用 | Initialization method, it is recommended to call in creator OnCreate
         * @param app 应用程序 | Application
         * @param activity 当前界面 | Current activity
         * @return true：成功 false：失败 | True: Success false: Failed
         */
        @JvmOverloads
        fun init(app: Application?, activity: Activity? = null): Boolean {

            var appTmp = app
            var activityTmp = activity

            // 两个参数都为null，直接抛异常 | Two parameters are null, throwing exceptions directly
            if (appTmp == null && activityTmp == null) {
                HMSAgentLog.e("the param of method HMSAgent.init can not be null !!!")
                return false
            }

            // 如果application实例为null，则从activity里面取 | If the creator instance is null, it is taken from the activity
            if (appTmp == null) {
                appTmp = activityTmp!!.application
            }

            // 如果application实例仍然为null，抛异常 | Throws an exception if the creator instance is still null
            if (appTmp == null) {
                HMSAgentLog.e("the param of method HMSAgent.init app can not be null !!!")
                return false
            }

            // activity 已经失效，则赋值null | Assignment NULL if activity has been invalidated
            if (activityTmp != null && activityTmp.isFinishing) {
                activityTmp = null
            }

            // 检查HMSAgent 和 HMSSDK 版本匹配关系 | Check hmsagent and HMSSDK version matching relationships
            if (!checkSDKVersion(appTmp)) {
                return false
            }

            HMSAgentLog.i("init HMSAgent " + CURVER + " with hmssdkver " + HuaweiApiAvailability.HMS_SDK_VERSION_CODE)

            // 初始化activity管理类 | Initializing Activity Management Classes
            ActivityMgr.INST.init(appTmp, activityTmp)

            // 初始化HuaweiApiClient管理类 | Initialize Huaweiapiclient Management class
            ApiClientMgr.INST.init(appTmp)

            return true
        }

        /**
         * 释放资源，这里一般不需要调用 | Frees resources, which are generally not required to call
         */
        fun destroy() {
            HMSAgentLog.i("destroy HMSAgent")
            ActivityMgr.INST.release()
            ApiClientMgr.INST.release()
        }

        /**
         * 连接HMS SDK， 可能拉起界面(包括升级引导等)，建议在第一个界面进行连接。 | Connecting to the HMS SDK may pull up the activity (including upgrade guard, etc.), and it is recommended that you connect in the first activity.
         * 此方法可以重复调用，没必要为了只调用一次做复杂处理 | This method can be called repeatedly, and there is no need to do complex processing for only one call at a time
         * 方法为异步调用，调用结果在主线程回调 | Method is called asynchronously, and the result is invoked in the main thread callback
         * @param activity 当前界面的activity， 不能传空 | Activity of the current activity, cannot be empty
         * @param callback 连接结果回调 | Connection Result Callback
         */
        fun connect(activity: Activity, callback: ConnectHandler?) {
            HMSAgentLog.i("start connect")
            ApiClientMgr.INST.connect(object :IClientConnectCallback{
                override fun onConnect(rst: Int, client: HuaweiApiClient?) {
                    if (callback != null) {
                        Handler(Looper.getMainLooper()).post { callback.onConnect(rst) }
                    }
                }
            },true)
        }

        /**
         * 检查本应用的升级 | Check for upgrades to this application
         * @param activity 上下文 | context
         * @param callback 升级结果回调 | check update Callback
         */
        fun checkUpdate(activity: Activity, callback: CheckUpdateHandler) {
            CheckUpdateApi().checkUpdate(activity, callback)
        }
    }

}
/**
 * 初始化方法，建议在Application onCreate里面调用    | Initialization method, it is recommended to call in creator OnCreate
 * @param app 应用程序                              | Application
 * @return true：成功 false：失败                   | True: Success false: Failed
 */
