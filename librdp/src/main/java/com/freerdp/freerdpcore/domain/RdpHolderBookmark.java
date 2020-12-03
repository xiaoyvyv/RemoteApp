package com.freerdp.freerdpcore.domain;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 占位标签
 */
public class RdpHolderBookmark extends BaseRdpBookmark {

    public static final Parcelable.Creator<RdpHolderBookmark> CREATOR =
            new Parcelable.Creator<RdpHolderBookmark>() {
                public RdpHolderBookmark createFromParcel(Parcel in) {
                    return new RdpHolderBookmark(in);
                }

                @Override
                public RdpHolderBookmark[] newArray(int size) {
                    return new RdpHolderBookmark[size];
                }
            };
    private String name;

    public RdpHolderBookmark(Parcel parcel) {
        super(parcel);
        type = TYPE_PLACEHOLDER;
        name = parcel.readString();
    }

    public RdpHolderBookmark() {
        super();
        type = TYPE_PLACEHOLDER;
        name = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(name);
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
