package com.chansos.ylg.huawei.module.main

import android.view.View
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewAdapter
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewHolder
import com.chansos.ylg.huawei.R


class AboutDialogMessageAdapter : BaseRecyclerViewAdapter<String>() {
    private var resource = Kt.App.getResources()
    private var labelList: Array<String> = resource.getStringArray(R.array.about_message_label_list)
    private var descriptionList: Array<String> = resource.getStringArray(R.array.about_message_description_list)

    override fun getRootLayoutResId(): Int {
        return R.layout.item_about_dialog_message_list
    }

    override fun onBind(viewHolder: BaseRecyclerViewHolder, data: String, position: Int) {
        viewHolder.setText(R.id.item_about_dialog_message_label, labelList[position])
        if (descriptionList.size > position) {
            viewHolder.setText(R.id.item_about_dialog_message_description, "：${descriptionList[position]}")
        }
    }

    override fun onViewCreate(view: View) {

    }
}