package com.huawei.android.hms.agent.common

/**
 * 工具类
 */
object StrUtils {
    /**
     * 返回对象的描述，这里为了避免用户数据隐私的泄露，只是返回对象本身的描述 类名@hashcode
     * @param object 对象
     * @return 对象的描述
     */
    fun objDesc(`object`: Any?): String {
        return if (`object` == null) "null" else `object`.javaClass.name + '@'.toString() + Integer.toHexString(`object`.hashCode())
    }
}
