package com.freerdp.freerdpcore.services;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.presentation.BookmarkActivity;
import com.freerdp.freerdpcore.presentation.SessionActivity;

public class SessionRequestHandlerActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void startSessionWithConnectionReference(String refStr) {
        Bundle bundle = new Bundle();
        bundle.putString(SessionActivity.PARAM_CONNECTION_REFERENCE, refStr);
        Intent sessionIntent = new Intent(this, SessionActivity.class);
        sessionIntent.putExtras(bundle);

        startActivityForResult(sessionIntent, 0);
    }

    private void editBookmarkWithConnectionReference(String refStr) {
        Bundle bundle = new Bundle();
        bundle.putString(BookmarkActivity.PARAM_CONNECTION_REFERENCE, refStr);
        Intent bookmarkIntent = new Intent(this.getApplicationContext(), BookmarkActivity.class);
        bookmarkIntent.putExtras(bundle);
        startActivityForResult(bookmarkIntent, 0);
    }

    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action))
            startSessionWithConnectionReference(ConnectionReference.getHostnameReference(
                    intent.getStringExtra(SearchManager.QUERY)));
        else if (Intent.ACTION_VIEW.equals(action))
            startSessionWithConnectionReference(intent.getDataString());
        else if (Intent.ACTION_EDIT.equals(action))
            editBookmarkWithConnectionReference(intent.getDataString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.setResult(resultCode);
        this.finish();
    }
}
