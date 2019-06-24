package com.chansos.ylg.huawei.api.web.url

import com.chansos.libs.rxkotlin.annotations.BaseUrl
import com.chansos.libs.rxkotlin.annotations.Domain
import com.chansos.ylg.huawei.model.web.url.TypeListModule
import io.reactivex.Observable
import retrofit2.http.GET

@Domain("https://official-file.oss-cn-shenzhen.aliyuncs.com/")
@BaseUrl("ylg/web_url/")
interface Type {
    @GET("type_list.json")
    fun list(): Observable<TypeListModule>
}