package com.freerdp.freerdpcore.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 快速连接标签
 */
public class RdpQuickBookmark extends RdpBookmark {

    public static final Parcelable.Creator<RdpQuickBookmark> CREATOR =
            new Parcelable.Creator<RdpQuickBookmark>() {
                public RdpQuickBookmark createFromParcel(Parcel in) {
                    return new RdpQuickBookmark(in);
                }

                @Override
                public RdpQuickBookmark[] newArray(int size) {
                    return new RdpQuickBookmark[size];
                }
            };

    public RdpQuickBookmark(Parcel parcel) {
        super(parcel);
        type = TYPE_QUICK_CONNECT;
    }

    public RdpQuickBookmark() {
        super();
        type = TYPE_QUICK_CONNECT;
    }
}
