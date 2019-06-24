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
import com.chansos.ylg.huawei.utils.U

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
        val list = data.list
        recyclerView.setItemViewCacheSize(list.size)
        val adapter = recyclerView.adapter as UrlListAdapter
        adapter.setDataList(U.convertList(list))
        adapter.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                onUrlItemClick?.onItemClick(list[position]!!)
            }
        }
    }

    override fun onViewCreate(view: View) {
        recyclerView = view.findViewById(R.id.url_type_child_item_list)
        recyclerView.layoutManager = object : GridLayoutManager(view.context, 2) {
            override fun canScrollVertically(): Boolean {
                return false
            }

            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        val urlListAdapter = UrlListAdapter(fragment)
        recyclerView.adapter = urlListAdapter
    }
}