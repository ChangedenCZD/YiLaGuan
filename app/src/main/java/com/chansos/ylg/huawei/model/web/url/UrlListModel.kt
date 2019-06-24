package com.chansos.ylg.huawei.model.web.url

import io.realm.RealmList
import io.realm.RealmObject

open class UrlListModel : RealmObject() {
    var list = RealmList<TypeItem>()
    var type = ""
    var expired = System.currentTimeMillis()
}

open class TypeItem : RealmObject() {
    var list = RealmList<UrlItem>()
    var title = ""
}

open class UrlItem : RealmObject() {
    var content = ""
    var href = ""
    var icon = ""
}