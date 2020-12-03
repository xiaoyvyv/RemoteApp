package com.freerdp.freerdpcore.domain;

import android.content.SharedPreferences;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }

    @Override
    public void writeToSharedPreferences(SharedPreferences sharedPrefs) {
        super.writeToSharedPreferences(sharedPrefs);
    }

    @Override
    public void readFromSharedPreferences(SharedPreferences sharedPrefs) {
        super.readFromSharedPreferences(sharedPrefs);
    }
}
