package io.github.kmenager.spotifystreamer;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import io.github.kmenager.spotifystreamer.adapters.SearchArtistAdapter;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import io.github.kmenager.spotifystreamer.views.MarginDecoration;
import kaaes.spotify.webapi.android.models.Artist;

public class SearchActivity extends AppCompatActivity implements GetArtistsFragment.GetArtistsCallbacks{


    public static final String ARTIST_INTENT = "ARTIST_INTENT";

    private Toolbar mToolbar;
    private EditText mEditSearch;

    private View mHeaderList;
    private RecyclerView mRecyclerViewArtist;
    private View mEmptyView;
    private ProgressBar mProgressBar;
    private View mErrorResultView;

    private static final String TAG_GET_ARTISTS_FRAGMENT = "GET_ARTISTs_FRAGMENT";
    private GetArtistsFragment mGetArtistsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_search);
        mEditSearch = (EditText) findViewById(R.id.edit_search);
        mErrorResultView = findViewById(R.id.no_result_view);
        TextView textNoResut = (TextView) mErrorResultView.findViewById(R.id.textview_no_result);
        textNoResut.setText(getString(R.string.no_result_found_text));
        mErrorResultView.setVisibility(View.INVISIBLE);
        mRecyclerViewArtist = (RecyclerView) findViewById(R.id.recycler_view_search);
        mRecyclerViewArtist.addItemDecoration(new MarginDecoration(this));

        mHeaderList = LayoutInflater.from(this).inflate(R.layout.header_list, mRecyclerViewArtist, false);
        final GridLayoutManager manager = (GridLayoutManager) mRecyclerViewArtist.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ((SearchArtistAdapter)mRecyclerViewArtist.getAdapter()).isHeader(position) ? manager.getSpanCount() : 1;
            }
        });

        mEmptyView = findViewById(R.id.recyclerview_search_empty);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_search);
        onInitToolbar();
        onInitEditText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }


        FragmentManager fm = getFragmentManager();
        mGetArtistsFragment = (GetArtistsFragment) fm.findFragmentByTag(TAG_GET_ARTISTS_FRAGMENT);

        if (mGetArtistsFragment == null) {
            mGetArtistsFragment = new GetArtistsFragment();
            fm.beginTransaction().add(mGetArtistsFragment, TAG_GET_ARTISTS_FRAGMENT).commit();
        }



        showProgressBar(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("keyword", mEditSearch.getText().toString());
    }

    private void onInitToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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

                    showEmptyView(false);
                    showProgressBar(true);
                    mGetArtistsFragment.getArtists(v.getText().toString());
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

    private void switchVisibilityEditSearch() {
        if (mEditSearch.getVisibility() == View.VISIBLE) {
            if (mEditSearch.getText().toString().isEmpty()) {
                mEditSearch.setVisibility(View.INVISIBLE);
                showKeyboard(false);
            } else {
                mEditSearch.setText("");
                setFocusEditText();
            }
            refreshAdapterArtists(null);
            showErrorResultView(false);
        } else {
            mEditSearch.setVisibility(View.VISIBLE);
            setFocusEditText();
            showKeyboard(true);
        }
        showEmptyView(true);
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
    private void showKeyboard(boolean visible) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (visible) {
            mgr.showSoftInput(mEditSearch, 0);
        } else {
            mgr.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);
        }
    }

    private void refreshAdapterArtists(List<Artist> artists) {
        if (mRecyclerViewArtist.getAdapter() == null) {
            ((TextView)mHeaderList).setText(R.string.header_text_search_artist);
            SearchArtistAdapter adapter = new SearchArtistAdapter(
                    this,
                    artists,
                    mHeaderList, new SearchArtistAdapter.SearchArtistAdapterOnClickHandler() {
                @Override
                public void onClick(ArtistData artistData, SearchArtistAdapter.SearchArtistAdapterHolder vh) {
                    onItemSelected(artistData, vh);
                }
            }
            );
            mRecyclerViewArtist.setAdapter(adapter);
        } else {
            ((SearchArtistAdapter) mRecyclerViewArtist.getAdapter()).reset(artists);
        }
        if (artists == null || artists.size() == 0) {
            mHeaderList.setVisibility(View.INVISIBLE);
            showErrorResultView(true);
        } else {
            mHeaderList.setVisibility(View.VISIBLE);
            showErrorResultView(false);
        }
    }

    protected void onItemSelected(ArtistData artistData, SearchArtistAdapter.SearchArtistAdapterHolder vh) {
        Intent intent = new Intent(this, ArtistActivity.class);
        intent.putExtra(ARTIST_INTENT, artistData);

        ActivityOptionsCompat activityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        new Pair<View, String>(vh.mImageViewIcon, getString(R.string.transition_image_key)));
        ActivityCompat.startActivity(SearchActivity.this, intent, activityOptions.toBundle());
    }

    private void showEmptyView(boolean state) {
        mEmptyView.setVisibility(state ? View.VISIBLE : View.GONE);

    }

    private void showProgressBar(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showErrorResultView(boolean visible) {
        mErrorResultView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(List<Artist> artists) {
        refreshAdapterArtists(artists);
        showKeyboard(false);
        showProgressBar(false);
    }
}