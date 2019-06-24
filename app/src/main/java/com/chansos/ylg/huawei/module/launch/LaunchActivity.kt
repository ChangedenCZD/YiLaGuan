package com.chansos.ylg.huawei.module.launch

import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.annotations.Autowire
import com.chansos.libs.rxkotlin.annotations.PageLayoutId
import com.chansos.libs.rxkotlin.annotations.PageOptions
import com.chansos.libs.rxkotlin.classes.BaseActivity
import com.chansos.libs.rxkotlin.utils.LogUtils
import com.chansos.ylg.huawei.R
import com.chansos.ylg.huawei.module.main.MainActivity
import java.util.*

@PageLayoutId(R.layout.activity_launch)
@PageOptions(orientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, theme = R.style.NoActionBarPage)
class LaunchActivity : BaseActivity(), LaunchContract.View {
    @Autowire
    private lateinit var presenter: LaunchPresenter

    override fun initialize() {
        presenter.checkRequestPermission()
    }

    override fun nextPage() {
        Kt.UI.quickTo(MainActivity::class.java, self)
        Kt.App.finish(self)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        LogUtils.i("xxx $requestCode ${Arrays.toString(permissions)} ${Arrays.toString(grantResults)}")
        nextPage()
    }
}

