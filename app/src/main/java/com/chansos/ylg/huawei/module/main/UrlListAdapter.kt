package com.chansos.ylg.huawei.module.main

import android.net.Uri
import android.view.View
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseFragment
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewAdapter
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewHolder
import com.chansos.ylg.huawei.R
import com.chansos.ylg.huawei.model.web.url.UrlItem

class UrlListAdapter(private var fragment: BaseFragment) : BaseRecyclerViewAdapter<UrlItem>() {

    override fun getRootLayoutResId(): Int {
        return R.layout.item_web_url_list
    }

    override fun onBind(viewHolder: BaseRecyclerViewHolder, data: UrlItem, position: Int) {
        viewHolder.setText(R.id.web_url_item_title, data.content)
        val uri = Uri.parse(data.href)
        Kt.Image.setDefaultImage(R.mipmap.ic_launcher)
        Kt.Image.setErrorImage(R.mipmap.ic_launcher)
        Kt.Image.load(
            viewHolder.get(R.id.web_url_item_icon),
            "${uri.scheme}://${uri.host}/favicon.ico",
            fragment
        )
        viewHolder.setImage(R.id.web_url_item_icon, "${uri.scheme}://${uri.host}/favicon.ico")
    }

    override fun onViewCreate(view: View) {

    }
}