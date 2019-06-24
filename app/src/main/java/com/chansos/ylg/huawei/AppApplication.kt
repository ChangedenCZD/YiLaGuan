package com.chansos.ylg.huawei

import androidx.multidex.MultiDexApplication
import com.chansos.libs.rxkotlin.Kt
import com.chansos.ylg.huawei.utils.RealmUtils
import com.huawei.android.hms.agent.HMSAgent

class AppApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        /**
         * 初始化应用管理工具
         * */
        Kt.App.init(this.applicationContext)
        RealmUtils.init(this)
        HMSAgent.init(this)
    }
}
