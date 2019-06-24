package com.chansos.ylg.huawei.module.main

import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseActivity
import com.chansos.libs.rxkotlin.utils.LogUtils
import com.chansos.ylg.huawei.api.web.url.Type
import com.chansos.ylg.huawei.model.web.url.TypeListModule
import com.chansos.ylg.huawei.utils.RealmUtils

class MainPresenter : MainContract.Presenter {
    private lateinit var view: MainContract.View
    override fun fetchTypeList() {
        try {
            fetchLocalData()
        } catch (e: Exception) {
            e.printStackTrace()
            fetchNetWorkData()
        }
    }

    private fun fetchLocalData() {
        val db = RealmUtils.defaultInstance
        RealmUtils.autoDelete(
            db, db.where(TypeListModule::class.java)
                .lessThan("expired", System.currentTimeMillis())
                .findAll()
        )
        val result = db
            .where(TypeListModule::class.java)
            .greaterThan("expired", System.currentTimeMillis())
            .findFirst()
        if (result != null && result.data.isNotEmpty()) {
            view.showTypeList(result.data)
            LogUtils.i("type data from local")
        } else {
            fetchNetWorkData()
        }
    }

    private fun fetchNetWorkData() {
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
            if (t.data.isNotEmpty()) {
                t.expired = System.currentTimeMillis() + 86400000
                RealmUtils.autoInsert(RealmUtils.defaultInstance, t)
                view.showTypeList(t.data)
                LogUtils.i("type data from network")
            }
        }

        override fun onError(e: Throwable) {
            super.onError(e)
            LogUtils.e(e)
        }
    }

    override fun exitApp() = Kt.App.exit()
}