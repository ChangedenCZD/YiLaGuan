package com.chansos.ylg.huawei.module.main

import com.alibaba.fastjson.JSON
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseActivity
import com.chansos.libs.rxkotlin.utils.LogUtils
import com.chansos.ylg.huawei.api.web.url.Type
import com.chansos.ylg.huawei.model.web.url.TypeListModule

class MainPresenter : MainContract.Presenter {
    private lateinit var view: MainContract.View
    override fun fetchTypeList() {
        try {
            Kt.Request
                .create<TypeListModule>(view as BaseActivity)
                .api(Kt.Request.api(Type::class.java).list())
                .obs(Obs(view as BaseActivity, view))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class Obs(activity: BaseActivity, val view: MainContract.View) : Kt.Observer<TypeListModule>(activity) {
        override fun onNext(t: TypeListModule) {
            LogUtils.d(JSON.toJSONString(t))
            if (t.data.isNotEmpty()) {
                view.showTypeList(t.data)
            }
        }

        override fun onError(e: Throwable) {
            super.onError(e)
            LogUtils.e(e)
        }
    }

    override fun exitApp() = Kt.App.exit()
}