package io.github.kmenager.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.kmenager.spotifystreamer.adapters.ArtistPageTopTrackAdapter;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import io.github.kmenager.spotifystreamer.model.TrackData;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistActivity extends AppCompatActivity {

    private View mHeaderList;

    private RecyclerView mRecyclerViewTopTrack;
    private View mErrorResultView;

    private SpotifyApi api = new SpotifyApi();
    private SpotifyService spotifyService = api.getService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mRecyclerViewTopTrack = (RecyclerView) findViewById(R.id.recycler_view_top_tracks);
        mRecyclerViewTopTrack.setHasFixedSize(true);

        final GridLayoutManager manager = new GridLayoutManager(this, 2);
        mRecyclerViewTopTrack.setLayoutManager(manager);

        mHeaderList = LayoutInflater.from(this).inflate(R.layout.header_list, mRecyclerViewTopTrack, false);

        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ((ArtistPageTopTrackAdapter)mRecyclerViewTopTrack.getAdapter()).isHeader(position) ?
                        manager.getSpanCount() : 1;
            }
        });

        mRecyclerViewTopTrack.setLayoutManager(new LinearLayoutManager(this));

        mErrorResultView = findViewById(R.id.no_result_view);
        TextView textNoResut = (TextView) mErrorResultView.findViewById(R.id.textview_no_result);
        textNoResut.setText(getString(R.string.no_top_tracks_found_text));
        mErrorResultView.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        ArtistData artistData = intent.getParcelableExtra(SearchActivity.ARTIST_INTENT);

        String artistId = artistData.getArtistId();
        String artistUrl = artistData.getUrlImage();
        String artistName = artistData.getName();


        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(artistName);
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        if (artistUrl == null) {
            imageView.setImageResource(R.drawable.ic_empty_artist);
        } else {
            Glide.with(this)
                    .load(artistUrl)
                    .placeholder(R.drawable.ic_empty_artist)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
        Map<String, Object> options = new HashMap<>(1);
        String locale = getResources().getConfiguration().locale.getISO3Country();

        options.put("country", locale.substring(0, 2));
        spotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                final List<Track> trackList = tracks.tracks;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshAdapter(trackList);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    private void refreshAdapter(List<Track> tracks) {
        if (mRecyclerViewTopTrack.getAdapter() == null) {
            ((TextView)mHeaderList).setText(R.string.header_text_top_tracks);
            ArtistPageTopTrackAdapter adapter = new ArtistPageTopTrackAdapter(
                    this,
                    tracks,
                    mHeaderList,
                    new ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler() {
                        @Override
                        public void onClick(TrackData trackData, ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterHolder vh) {
                            Snackbar.make(mRecyclerViewTopTrack, trackData.getName(), Snackbar.LENGTH_LONG).show();
                        }
                    });
            mRecyclerViewTopTrack.setAdapter(adapter);
        } else {
            ((ArtistPageTopTrackAdapter) mRecyclerViewTopTrack.getAdapter()).reset(tracks);
        }

        if (tracks == null || tracks.size() == 0) {
            mHeaderList.setVisibility(View.INVISIBLE);
            showErrorResultView(true);
        } else {
            mHeaderList.setVisibility(View.VISIBLE);
            showErrorResultView(false);
        }
    }

    private void showErrorResultView(boolean visible) {
        mErrorResultView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /*
    private void onInitToolbar() {

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    public void showProgressBar(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void showTitle(boolean visible) {
        mHeaderArtist.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mTitleTopTrack.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void loadTitle(String urlImage, String name) {
        Picasso.with(this)
                .load(urlImage)
                .into(mTitleArtistImage);
        mTitleArtistName.setText(name);
    }
    */
}
