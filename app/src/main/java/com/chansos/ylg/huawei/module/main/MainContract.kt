package com.chansos.ylg.huawei.module.main

import com.chansos.libs.rxkotlin.classes.BaseContract
import com.chansos.ylg.huawei.model.web.url.TypeListModuleItem
import com.chansos.ylg.huawei.model.web.url.TypeItem
import io.realm.RealmList

interface MainContract : BaseContract {
    interface View : BaseContract.BaseView {
        fun showTypeList(typeList: RealmList<TypeListModuleItem>)

    }

    interface Presenter : BaseContract.BasePresenter {
        fun fetchTypeList()
        fun exitApp()

    }

    interface WebTypeView : BaseContract.BaseView {
        fun showUrlList(list: RealmList<TypeItem>)

    }

    interface WebTypePresenter : BaseContract.BasePresenter {
        fun bind(pageType: String, view: WebTypeView)
        fun fetchData(pageType: String)

    }
}