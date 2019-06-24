package com.chansos.ylg.huawei.api.web.url

import com.chansos.libs.rxkotlin.annotations.BaseUrl
import com.chansos.libs.rxkotlin.annotations.Domain
import com.chansos.ylg.huawei.model.web.url.TypeListModule
import com.chansos.ylg.huawei.model.web.url.UrlListModel
import io.reactivex.Observable
import retrofit2.http.GET

@Domain("https://official-file.oss-cn-shenzhen.aliyuncs.com/")
@BaseUrl("ylg/web_url/")
interface Url {
    @GET("type_list_index.json")
    fun list(): Observable<TypeListModule>

    @GET("index_url_list.json")
    fun index(): Observable<UrlListModel>

    @GET("android_url_list.json")
    fun android(): Observable<UrlListModel>

    @GET("ios_url_list.json")
    fun ios(): Observable<UrlListModel>

    @GET("tools_url_list.json")
    fun tools(): Observable<UrlListModel>

    @GET("web_url_list.json")
    fun web(): Observable<UrlListModel>

    @GET("server_url_list.json")
    fun server(): Observable<UrlListModel>

    @GET("design_url_list.json")
    fun design(): Observable<UrlListModel>
}