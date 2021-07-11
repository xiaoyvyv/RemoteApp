package com.xiaoyv.ui.scroll

import android.view.View
import me.everything.android.ui.overscroll.HorizontalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator

/**
 * OverScrollDecoratorHelper
 *
 * @author why
 * @since 2021/07/11
 **/
object OverScrollDecoratorHelperExt {

    @JvmStatic
    fun setUpOverScroll(
        scrollView: View,
        orientation: Int
    ): IOverScrollDecor {
        return when (orientation) {
            OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL -> HorizontalOverScrollBounceEffectDecorator(
                OverScrollDecorAdapter(scrollView)
            )
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL -> VerticalOverScrollBounceEffectDecorator(
                OverScrollDecorAdapter(scrollView)
            )
            else -> throw IllegalArgumentException("orientation")
        }
    }
}

