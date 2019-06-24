package com.chansos.ylg.huawei.module.main


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.chansos.libs.rxkotlin.Kt
import com.chansos.libs.rxkotlin.annotations.Autowire
import com.chansos.libs.rxkotlin.annotations.PageLayoutId
import com.chansos.libs.rxkotlin.classes.BaseViewPagerFragment
import com.chansos.ylg.huawei.R
import com.chansos.ylg.huawei.model.web.url.TypeItem
import com.chansos.ylg.huawei.model.web.url.UrlItem
import com.chansos.ylg.huawei.utils.U
import kotlinx.android.synthetic.main.fragment_web_type.*


@PageLayoutId(R.layout.fragment_web_type)
class WebTypeFragment : BaseViewPagerFragment(), MainContract.WebTypeView, UrlTypeChildListAdapter.OnUrlItemClick {

    companion object {
        const val CODE_REQUEST_BROWSER = 1
    }

    @Autowire
    private lateinit var presenter: WebTypePresenter

    private lateinit var pageTitle: String
    private lateinit var pageType: String

    private lateinit var urlTypeChildListAdapter: UrlTypeChildListAdapter

    override fun onInitialize() {
        urlTypeChildListAdapter = UrlTypeChildListAdapter(self)
        url_list_layout.adapter = urlTypeChildListAdapter
        url_list_layout.layoutManager = LinearLayoutManager(activity)
        urlTypeChildListAdapter.onUrlItemClick = this
    }

    override fun onFirstTime() {
        super.onFirstTime()

        pageTitle = this.arguments?.getString("title") ?: ""
        pageType = this.arguments?.getString("type") ?: ""

        presenter.fetchData(pageType)
    }

    override fun showUrlList(list: ArrayList<TypeItem>) {
        urlTypeChildListAdapter.setDataList(list)
    }

    override fun onItemClick(data: UrlItem) {
        val title = data.content
        MaterialDialog
            .Builder(this.activity as Context)
            .title(title)
            .items(R.array.url_item_operations)
            .itemsCallback { _, _, position, _ ->
                val url = data.href
                when (position) {
                    0 -> {
                        U.browser(self, url)
                    }
                    1 -> {
                        addUrl(title, url)
                    }
                }
            }
            .build()
            .show()
    }

    private fun addUrl(title: String, url: String) {
        try {
            val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(title, url)
            clipboardManager.setPrimaryClip(clipData)
            Kt.UI.showToast(R.string.url_item_copy_success)
        } catch (e: Exception) {
            Kt.UI.showToast(R.string.url_item_copy_fail)
        }
    }

}
