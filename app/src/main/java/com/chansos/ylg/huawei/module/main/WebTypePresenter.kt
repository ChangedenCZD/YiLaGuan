package com.chansos.ylg.huawei.module.main

import com.alibaba.fastjson.JSON
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseFragment
import com.chansos.libs.rxkotlin.utils.LogUtils
import com.chansos.ylg.huawei.api.web.url.Url
import com.chansos.ylg.huawei.model.web.url.UrlListModel
import io.reactivex.Observable

class WebTypePresenter : MainContract.WebTypePresenter {

    private lateinit var view: MainContract.WebTypeView

    @Suppress("UNCHECKED_CAST")
    override fun fetchData(pageType: String) {
        try {
            Kt.Request
                .create<UrlListModel>(view as BaseFragment)
                .api(
                    Url::class.java.getMethod(pageType)
                        .invoke(Kt.Request.api(Url::class.java)) as Observable<UrlListModel>
                )
                .obs(Obs(view as BaseFragment, view))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    class Obs(fragment: BaseFragment, val view: MainContract.WebTypeView) : Kt.Observer<UrlListModel>(fragment) {
        override fun onNext(t: UrlListModel) {
            LogUtils.d(JSON.toJSONString(t))
            view.showUrlList(t.list)
        }

        override fun onError(e: Throwable) {
            super.onError(e)
            LogUtils.e(e)
        }
    }
}