package com.chansos.ylg.huawei.module.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.annotations.Autowire
import com.chansos.libs.rxkotlin.annotations.PageLayoutId
import com.chansos.libs.rxkotlin.classes.BaseActivity
import com.chansos.libs.rxkotlin.classes.BaseRecyclerViewAdapter
import com.chansos.ylg.huawei.R
import com.chansos.ylg.huawei.model.web.url.TypeListModuleItem
import com.chansos.ylg.huawei.utils.U
import com.google.android.material.tabs.TabLayout
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*

@PageLayoutId(R.layout.activity_main)
class MainActivity : BaseActivity(), MainContract.View, ViewPager.OnPageChangeListener,
    TabLayout.OnTabSelectedListener {

    @Autowire
    private lateinit var presenter: MainPresenter

    private var webTypeFragmentList = ArrayList<WebTypeFragment>()
    private lateinit var viewPagerAdapter: Adapter

    override fun initialize() {
        viewPagerAdapter = Adapter(supportFragmentManager)
        view_pager.adapter = viewPagerAdapter
        view_pager.addOnPageChangeListener(this)
        tab_layout.addOnTabSelectedListener(this)

        presenter.fetchTypeList()
    }

    override fun showTypeList(typeList: RealmList<TypeListModuleItem>) {
        val webTypePresenter = WebTypePresenter()
        typeList.forEach {
            val fragment = WebTypeFragment()
            val arguments = Bundle()
            val type = it.type!!
            arguments.putString("title", it.title)
            arguments.putString("type", type)
            fragment.arguments = arguments
            fragment.presenter = webTypePresenter
            webTypePresenter.bind(type, fragment)
            webTypeFragmentList.add(fragment)
        }
        viewPagerAdapter.setList(webTypeFragmentList)
        view_pager.offscreenPageLimit = webTypeFragmentList.size
        setTabLayout(typeList)
        onPageSelected(0)
    }

    private fun setTabLayout(typeList: RealmList<TypeListModuleItem>) {
        viewPagerAdapter.setList(webTypeFragmentList)
        tab_layout.setupWithViewPager(view_pager)
        for (i in 0 until tab_layout.tabCount) {
            val tab = tab_layout.getTabAt(i)
            if (tab != null) {
                tab.setCustomView(R.layout.item_url_type_tab)
                tab.customView?.findViewById<TextView>(R.id.url_type_tab_item_label)?.text = typeList[i]?.title
            }
        }
    }

    class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private var webTypeFragmentList = ArrayList<WebTypeFragment>()

        fun setList(list: ArrayList<WebTypeFragment>) {
            webTypeFragmentList.clear()
            webTypeFragmentList.addAll(list)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return webTypeFragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return webTypeFragmentList[position]
        }

    }

    override fun onPageScrollStateChanged(p0: Int) {
    }

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
    }

    override fun onPageSelected(position: Int) {
        view_pager.setCurrentItem(position, false)
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabSelected(p0: TabLayout.Tab?) {
        onPageSelected(p0!!.position)
    }

    @SuppressLint("InflateParams")
    private fun showAboutDialog() {
        val view = LayoutInflater.from(self).inflate(R.layout.dialog_about, null)
        val recyclerView = Kt.UI.get<RecyclerView>(view, R.id.about_dialog_message_list)
        recyclerView.layoutManager = LinearLayoutManager(self)
        val adapter = AboutDialogMessageAdapter()
        val array = Kt.App.getResources().getStringArray(R.array.about_message_label_list)
        val urlList = Kt.App.getResources().getStringArray(R.array.about_message_url_list)
        val list = ArrayList<String>()
        list.addAll(array.asList())
        adapter.setDataList(list)
        recyclerView.adapter = adapter
        adapter.onItemClickListener = object : BaseRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                if (urlList.size > position) {
                    U.browser(self, urlList[position])
                }
            }
        }
        MaterialDialog.Builder(self)
            .title(R.string.about)
            .customView(view, true)
            .build()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.item_about_menu -> showAboutDialog()
            R.id.item_exit_menu -> presenter.exitApp()
        }
        return true
    }
}
