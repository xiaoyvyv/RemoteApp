package com.xiaoyv.mine.main.contract;

/**
 * MineContract
 *
 * @author why
 * @since 2020/12/10
 **/
public interface MineContract {
    interface Adapter {
        /**
         * 调换位置
         *
         * @param fromPos 从
         * @param toPos   至
         */
        void onItemChange(int fromPos, int toPos);

        /**
         * 删除条目
         *
         * @param pos 位置
         */
        void onItemDelete(int pos);
    }
}
