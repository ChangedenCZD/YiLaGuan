package com.chansos.ylg.huawei.utils

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmResults

object RealmUtils {

    lateinit var defaultInstance: Realm

    fun init(context: Context) {
        Realm.init(context)
        Realm.setDefaultConfiguration(
            RealmConfiguration
                .Builder()
                .name("default")
                .build()
        )
        defaultInstance = Realm.getDefaultInstance()
    }

    fun getInstance(name: String): Realm {
        return Realm.getInstance(
            RealmConfiguration
                .Builder()
                .name(name)
                .build()
        )
    }

    fun autoInsert(db: Realm, obj: RealmModel): Boolean {
        return try {
            db.beginTransaction()
            db.insertOrUpdate(obj)
            db.commitTransaction()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun autoDelete(db: Realm, obj: RealmResults<out RealmModel>): Boolean {
        return try {
            db.beginTransaction()
            obj.deleteAllFromRealm()
            db.commitTransaction()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
