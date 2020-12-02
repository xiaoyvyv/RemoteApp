package com.freerdp.freerdpcore.domain;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class QuickConnectBookmark extends ManualBookmark {

    public static final Parcelable.Creator<QuickConnectBookmark> CREATOR =
            new Parcelable.Creator<QuickConnectBookmark>() {
                public QuickConnectBookmark createFromParcel(Parcel in) {
                    return new QuickConnectBookmark(in);
                }

                @Override
                public QuickConnectBookmark[] newArray(int size) {
                    return new QuickConnectBookmark[size];
                }
            };

    public QuickConnectBookmark(Parcel parcel) {
        super(parcel);
        type = TYPE_QUICK_CONNECT;
    }

    public QuickConnectBookmark() {
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
