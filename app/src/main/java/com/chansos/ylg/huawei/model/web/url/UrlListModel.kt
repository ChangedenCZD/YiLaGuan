package com.chansos.ylg.huawei.model.web.url

data class UrlListModel(
    val list: ArrayList<TypeItem>
)

data class TypeItem(
    val list: ArrayList<UrlItem>,
    val title: String
)

data class UrlItem(
    val content: String,
    val href: String
)