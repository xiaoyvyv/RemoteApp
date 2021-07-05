package com.freerdp.freerdpcore.utils

/**
 * Number
 *
 * @author why
 * @since 2021/07/05
 **/
fun Long?.orEmpty() = this ?: 0L
fun Int?.orEmpty() = this ?: 0
