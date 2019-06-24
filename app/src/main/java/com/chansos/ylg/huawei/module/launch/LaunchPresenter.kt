package com.chansos.ylg.huawei.module.launch

import android.Manifest
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseActivity

class LaunchPresenter : LaunchContract.Presenter {
    private lateinit var view: LaunchContract.View
    override fun checkRequestPermission() {
        if (Kt.Permission.check(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            view.nextPage()
        } else {
            Kt.Permission.request(view as BaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}