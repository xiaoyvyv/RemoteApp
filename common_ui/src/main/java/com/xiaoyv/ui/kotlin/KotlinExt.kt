package com.xiaoyv.ui.kotlin

import com.blankj.utilcode.util.Utils
import me.jessyan.autosize.utils.AutoSizeUtils

/**
 * KotlinExt
 *
 * @author why
 * @since 2021/07/04
 **/
class KotlinExt {

}

fun Float?.dp() = AutoSizeUtils.dp2px(Utils.getApp(), this ?: 0f)
fun Int?.dp() = AutoSizeUtils.dp2px(Utils.getApp(), this?.toFloat() ?: 0f)

fun Long?.orEmpty() = this ?: 0L
fun Int?.orEmpty() = this ?: 0
