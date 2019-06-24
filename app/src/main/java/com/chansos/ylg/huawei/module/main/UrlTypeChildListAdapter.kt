package com.chansos.ylg.huawei.module.main

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chansos.libs.rxkotlin.classes.BaseFragment
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewAdapter
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewHolder
import com.chansos.ylg.huawei.R
import com.chansos.ylg.huawei.model.web.url.TypeItem
import com.chansos.ylg.huawei.model.web.url.UrlItem

class UrlTypeChildListAdapter(private var fragment: BaseFragment) : BaseRecyclerViewAdapter<TypeItem>() {

    interface OnUrlItemClick {
        fun onItemClick(data: UrlItem)
    }

    private lateinit var recyclerView: RecyclerView

    var onUrlItemClick: OnUrlItemClick? = null


    override fun getRootLayoutResId(): Int {
        return R.layout.item_url_type_child_list
    }

    override fun onBind(viewHolder: BaseRecyclerViewHolder, data: TypeItem, position: Int) {
        viewHolder.setText(R.id.url_type_child_item_title, data.title)
        if (recyclerView.adapter == null) {
            val urlListAdapter = UrlListAdapter(fragment)
            recyclerView.adapter = urlListAdapter
            urlListAdapter.onItemClickListener = object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onUrlItemClick?.onItemClick(data.list[position])
                }

            }
        }
        (recyclerView.adapter as UrlListAdapter).setDataList(data.list)

    }

    override fun onViewCreate(view: View) {
        recyclerView = view.findViewById(R.id.url_type_child_item_list)
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
    }
}