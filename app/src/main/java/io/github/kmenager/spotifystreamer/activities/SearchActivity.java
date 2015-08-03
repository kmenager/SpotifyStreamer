package io.github.kmenager.spotifystreamer.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.fragments.SearchArtistFragment;

public class SearchActivity extends AppCompatActivity implements SpotifyRequest.SpotifyRequestCallback {


    public static final String ARTIST_INTENT = "ARTIST_INTENT";
    private SearchArtistFragment mSearchArtistFragment;

    private static final String TAG_SPOTIFY_REQUEST_FRAGMENT = "SPOTIFY_REQUEST_FRAGMENT";
    private SpotifyRequest mSpotifyRequest;
    private Toolbar mToolbar;
    private EditText mEditSearch;
    private String mKeyword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_search);
        mEditSearch = (EditText) findViewById(R.id.edit_search);
        
        FragmentManager fm = getSupportFragmentManager();
        mSearchArtistFragment = (SearchArtistFragment) fm.findFragmentById(R.id.fragment_search);
        if (mSearchArtistFragment == null) {
            mSearchArtistFragment = SearchArtistFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragment_search, mSearchArtistFragment)
                    .commit();
        }

        onInitToolbar();
        onInitEditText();

        mSpotifyRequest = (SpotifyRequest) fm.findFragmentByTag(TAG_SPOTIFY_REQUEST_FRAGMENT);

        if (mSpotifyRequest == null) {
            mSpotifyRequest = new SpotifyRequest();
            fm.beginTransaction().add(mSpotifyRequest, TAG_SPOTIFY_REQUEST_FRAGMENT).commit();
        }
    }

    private void onInitToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void onInitEditText() {
        setFocusEditText();
        mEditSearch.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchArtistFragment.showEmptyView(false);
                    mSearchArtistFragment.showProgressBar(true);
                    mKeyword = v.getText().toString();
                    mSpotifyRequest.getArtists(mKeyword);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mEditSearch.getVisibility() == View.VISIBLE) {
            menu.findItem(R.id.action_collapse_keyboard).setIcon(R.drawable.ic_close_white);
        } else {
            menu.findItem(R.id.action_collapse_keyboard).setIcon(R.drawable.ic_search_white);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_collapse_keyboard) {
            switchVisibilityEditSearch();
            invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchVisibilityEditSearch() {
        if (mEditSearch.getVisibility() == View.VISIBLE) {
            if (mEditSearch.getText().toString().isEmpty()) {
                mEditSearch.setVisibility(View.INVISIBLE);
                showKeyboard(false);
            } else {
                mEditSearch.setText("");
                setFocusEditText();
            }
            mSearchArtistFragment.refreshAdapterArtists(null);
            mSearchArtistFragment.showErrorResultView(false);
        } else {
            mEditSearch.setVisibility(View.VISIBLE);
            setFocusEditText();
            showKeyboard(true);
        }
        mSearchArtistFragment.showEmptyView(true);
    }

    private void setFocusEditText() {
        mEditSearch.requestFocus();
        mEditSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(true);
            }
        }
                , 200);
    }
    public void showKeyboard(boolean visible) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (visible) {
            mgr.showSoftInput(mEditSearch, 0);
        } else {
            mgr.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(List lists) {
        mSearchArtistFragment.refreshAdapterArtists(lists);
        showKeyboard(false);
        mSearchArtistFragment.showProgressBar(false);
    }
}