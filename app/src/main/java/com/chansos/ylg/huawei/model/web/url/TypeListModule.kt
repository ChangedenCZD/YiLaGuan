package com.chansos.ylg.huawei.model.web.url

import io.realm.RealmList
import io.realm.RealmObject

open class TypeListModule : RealmObject() {

    var `data` = RealmList<TypeListModuleItem>()
    var expired = System.currentTimeMillis()
}

open class TypeListModuleItem : RealmObject() {

    var title: String? = ""
    var type: String? = ""
}