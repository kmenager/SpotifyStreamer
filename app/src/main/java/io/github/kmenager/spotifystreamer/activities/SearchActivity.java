package io.github.kmenager.spotifystreamer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.kmenager.spotifystreamer.MusicService;
import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.fragments.MediaPlayerDialogFragment;
import io.github.kmenager.spotifystreamer.fragments.SearchArtistFragment;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import io.github.kmenager.spotifystreamer.model.TrackData;

public class SearchActivity extends AppCompatActivity implements SpotifyRequest.SpotifyRequestCallback {


    public static final String ARTIST_INTENT = "ARTIST_INTENT";
    private SearchArtistFragment mSearchArtistFragment;

    private static final String TAG_SPOTIFY_REQUEST_FRAGMENT = "SPOTIFY_REQUEST_FRAGMENT";
    private SpotifyRequest mSpotifyRequest;
    private Toolbar mToolbar;
    private EditText mEditSearch;
    private String mKeyword;
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_search);
        mEditSearch = (EditText) findViewById(R.id.edit_search);

        mTwoPane = getResources().getBoolean(R.bool.is_two_pane);

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
                    if (mTwoPane) {
                        if (mKeyword != null && !mKeyword.equals(v.getText().toString())) {
                            Fragment fragment = getSupportFragmentManager()
                                    .findFragmentByTag(ArtistActivity.TAG_ARTIST_FRAGMENT);
                            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        }
                    }
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_playing) {
            if (MusicService.getInstance() != null) {
                ArrayList<TrackData> trackDatas = MusicService.getInstance().getTracks();
                int currentPosition = MusicService.getInstance().getPosition();
                ArtistData artistData = MusicService.getInstance().getArtistData();

                if (trackDatas != null && trackDatas.size() > 0) {
                    FragmentManager fm = getSupportFragmentManager();
                    MediaPlayerDialogFragment player = MediaPlayerDialogFragment.newInstance(trackDatas,
                            currentPosition, artistData);

                    player.show(fm, MediaPlayerDialogFragment.TAG_MEDIA_PLAYER);

                    return true;
                }
            } else {
                Toast.makeText(this, R.string.error_no_music_playing, Toast.LENGTH_LONG).show();
            }

        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


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