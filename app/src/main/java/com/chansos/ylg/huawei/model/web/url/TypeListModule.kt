package com.chansos.ylg.huawei.model.web.url

data class TypeListModule(
    val `data`: ArrayList<Type>
)

data class Type(
    val title: String,
    val type: String
)