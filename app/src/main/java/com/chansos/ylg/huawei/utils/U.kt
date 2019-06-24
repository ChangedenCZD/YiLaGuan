package com.chansos.ylg.huawei.utils

import android.content.Intent
import android.net.Uri
import com.chansos.libs.rxkotlin.classes.BaseActivity
import com.chansos.libs.rxkotlin.classes.BaseFragment
import com.chansos.ylg.huawei.module.main.WebTypeFragment

class U {
    companion object {
        fun browser(activity: BaseActivity, url: String) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
//        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity")
            activity.startActivityForResult(intent, WebTypeFragment.CODE_REQUEST_BROWSER)
        }

        fun browser(fragment: BaseFragment, url: String) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
//        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity")
            fragment.startActivityForResult(intent, WebTypeFragment.CODE_REQUEST_BROWSER)
        }
    }
}