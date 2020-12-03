package com.freerdp.freerdpcore.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.domain.RdpBookmark;
import com.freerdp.freerdpcore.domain.RdpHolderBookmark;
import com.freerdp.freerdpcore.presentation.BookmarkActivity;
import com.xiaoyv.librdp.R;

import java.util.List;

public class BookmarkArrayAdapter extends ArrayAdapter<BaseRdpBookmark> {

    public BookmarkArrayAdapter(Context context, int textViewResourceId, List<BaseRdpBookmark> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View curView = convertView;
        if (curView == null) {
            LayoutInflater vi =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            curView = vi.inflate(R.layout.bookmark_list_item, null);
        }

        BaseRdpBookmark bookmark = getItem(position);
        TextView label = (TextView) curView.findViewById(R.id.bookmark_text1);
        TextView hostname = (TextView) curView.findViewById(R.id.bookmark_text2);
        ImageView star_icon = (ImageView) curView.findViewById(R.id.bookmark_icon2);
        assert label != null;
        assert hostname != null;

        label.setText(bookmark.getLabel());
        star_icon.setVisibility(View.VISIBLE);

        String refStr;
        if (bookmark.getType() == BaseRdpBookmark.TYPE_MANUAL) {
            hostname.setText(bookmark.<RdpBookmark>get().getHostname());
            refStr = ConnectionReference.getManualBookmarkReference(bookmark.getId());
            star_icon.setImageResource(R.drawable.icon_star_on);
        } else if (bookmark.getType() == BaseRdpBookmark.TYPE_QUICK_CONNECT) {
            // just set an empty hostname (with a blank) - the hostname is already displayed in the
            // label and in case we just set it to "" the textview will shrunk
            hostname.setText(" ");
            refStr = ConnectionReference.getHostnameReference(bookmark.getLabel());
            star_icon.setImageResource(R.drawable.icon_star_off);
        } else if (bookmark.getType() == BaseRdpBookmark.TYPE_PLACEHOLDER) {
            hostname.setText(" ");
            refStr = ConnectionReference.getPlaceholderReference(
                    bookmark.<RdpHolderBookmark>get().getName());
            star_icon.setVisibility(View.GONE);
        } else {
            // unknown bookmark type...
            refStr = "";
            assert false;
        }

        star_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start bookmark editor
                Bundle bundle = new Bundle();
                String refStr = v.getTag().toString();
                bundle.putString(BookmarkActivity.PARAM_CONNECTION_REFERENCE, refStr);

                Intent bookmarkIntent = new Intent(getContext(), BookmarkActivity.class);
                bookmarkIntent.putExtras(bundle);
                getContext().startActivity(bookmarkIntent);
            }
        });

        curView.setTag(refStr);
        star_icon.setTag(refStr);

        return curView;
    }

    public void addItems(List<BaseRdpBookmark> newItems) {
        for (BaseRdpBookmark item : newItems)
            add(item);
    }

    public void replaceItems(List<BaseRdpBookmark> newItems) {
        clear();
        for (BaseRdpBookmark item : newItems)
            add(item);
    }

    public void remove(long bookmarkId) {
        for (int i = 0; i < getCount(); i++) {
            BaseRdpBookmark bm = getItem(i);
            if (bm.getId() == bookmarkId) {
                remove(bm);
                return;
            }
        }
    }
}
