package com.chansos.ylg.huawei.module.main

import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseFragment
import com.chansos.libs.rxkotlin.utils.LogUtils
import com.chansos.ylg.huawei.api.web.url.Url
import com.chansos.ylg.huawei.model.web.url.UrlListModel
import com.chansos.ylg.huawei.utils.RealmUtils
import io.reactivex.Observable
import java.util.concurrent.ConcurrentHashMap

class WebTypePresenter : MainContract.WebTypePresenter {

    private val viewMap = ConcurrentHashMap<String, MainContract.WebTypeView>()

    override fun bind(pageType: String, view: MainContract.WebTypeView) {
        viewMap[pageType] = view
    }

    @Suppress("UNCHECKED_CAST")
    override fun fetchData(pageType: String) {
        val view = viewMap[pageType]
        if (view != null) {
            try {
                fetchLocalData(pageType, view)
            } catch (e: Exception) {
                e.printStackTrace()
                fetchNetWorkData(pageType, view)
            }
        }
    }

    private fun fetchLocalData(pageType: String, view: MainContract.WebTypeView) {
        val db = RealmUtils.defaultInstance
        RealmUtils.autoDelete(
            db, db.where(UrlListModel::class.java)
                .lessThan("expired", System.currentTimeMillis())
                .equalTo("type", pageType)
                .findAll()
        )
        val result = db
            .where(UrlListModel::class.java)
            .greaterThan("expired", System.currentTimeMillis())
            .equalTo("type", pageType)
            .findFirst()
        if (result != null) {
            view.showUrlList(result.list)
            LogUtils.i("url data from local")
        } else {
            fetchNetWorkData(pageType, view)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchNetWorkData(pageType: String, view: MainContract.WebTypeView) {
        try {
            Kt.Request
                .create<UrlListModel>(view as BaseFragment)
                .api(
                    Url::class.java.getMethod(pageType)
                        .invoke(Kt.Request.api(Url::class.java)) as Observable<UrlListModel>
                )
                .obs(Obs(view as BaseFragment, view, pageType))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    class Obs(fragment: BaseFragment, val view: MainContract.WebTypeView, private val pageType: String) :
        Kt.Observer<UrlListModel>(fragment) {
        override fun onNext(t: UrlListModel) {
            t.expired = System.currentTimeMillis() + 86400000
            t.type = pageType
            RealmUtils.autoInsert(RealmUtils.defaultInstance, t)
            view.showUrlList(t.list)
            LogUtils.i("url data from network")
        }

        override fun onError(e: Throwable) {
            super.onError(e)
            LogUtils.e(e)
        }
    }
}