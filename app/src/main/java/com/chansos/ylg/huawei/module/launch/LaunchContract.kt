package com.chansos.ylg.huawei.module.launch

import com.chansos.libs.rxkotlin.classes.BaseContract

interface LaunchContract:BaseContract{
    interface View : BaseContract.BaseView{
        fun nextPage()
    }

    interface Presenter : BaseContract.BasePresenter {
        fun checkRequestPermission()
    }
}