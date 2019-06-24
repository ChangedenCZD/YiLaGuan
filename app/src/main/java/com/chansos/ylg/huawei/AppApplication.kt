package com.chansos.ylg.huawei

import androidx.multidex.MultiDexApplication
import com.chansos.libs.rxkotlin.Kt
//import com.huawei.android.hms.agent.HMSAgent

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate", "unused")
class AppApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        /**
         * 初始化应用管理工具
         * */
        Kt.App.init(this.applicationContext)
//        HMSAgent.init(this)
    }
}
