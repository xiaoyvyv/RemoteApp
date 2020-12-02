package com.freerdp.freerdpcore.domain;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

public class PlaceholderBookmark extends BookmarkBase {

    public static final Parcelable.Creator<PlaceholderBookmark> CREATOR =
            new Parcelable.Creator<PlaceholderBookmark>() {
                public PlaceholderBookmark createFromParcel(Parcel in) {
                    return new PlaceholderBookmark(in);
                }

                @Override
                public PlaceholderBookmark[] newArray(int size) {
                    return new PlaceholderBookmark[size];
                }
            };
    private String name;

    public PlaceholderBookmark(Parcel parcel) {
        super(parcel);
        type = TYPE_PLACEHOLDER;
        name = parcel.readString();
    }

    public PlaceholderBookmark() {
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
