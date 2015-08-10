package io.github.kmenager.spotifystreamer.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.kmenager.spotifystreamer.MusicService;
import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.SettingsActivity;
import io.github.kmenager.spotifystreamer.SpotifySingleton;
import io.github.kmenager.spotifystreamer.adapters.ArtistPageTopTrackAdapter;
import io.github.kmenager.spotifystreamer.model.TrackData;
import io.github.kmenager.spotifystreamer.utils.NetworkHelper;
import io.github.kmenager.spotifystreamer.utils.PreferenceHelper;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ArtistFragment extends Fragment {


    private static final String TAG_SPOTIFY_REQUEST_FRAGMENT = "SPOTIFY_REQUEST_FRAGMENT_TRACK";
    private static final String ARTIST_NAME = "ARTIST_NAME";
    private static final String ARTIST_URL = "ARTIST_URL";
    private static final String ARTIST_ID = "ARTIST_ID";
    private View mHeaderList;
    private RecyclerView mRecyclerViewTopTrack;
    private View mErrorResultView;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mImageView;
    private boolean mTwoPane;
    private ArrayList<TrackData> mTracks = new ArrayList<>();
    private String mArtistId;
    private MenuItem mMenuShare;
    private ShareActionProvider provider;
    LocalBroadcastManager mLocalBroadcastManager;


    private MediaPlayerDialogFragment mPlayer;


    private ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler mHandler =
            new ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler() {
                @Override
                public void onClick(TrackData trackData, ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterHolder vh) {
                    if (NetworkHelper.isOnline(getActivity())) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();

                        mPlayer = MediaPlayerDialogFragment.newInstance(mTracks, vh.getLayoutPosition() - 1);
                        mPlayer.show(fm, MediaPlayerDialogFragment.TAG_MEDIA_PLAYER);
                        Intent audioService = new Intent(getActivity(), MusicService.class);
                        getActivity().getApplicationContext().startService(audioService);
                    } else {
                        Toast.makeText(getActivity(), "No internet Available", Toast.LENGTH_SHORT).show();
                    }
                }
            };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.onAudioStop();
        }
    }

    public static ArtistFragment newInstance(String artistId, String artistName, String artistUrl) {

        Bundle args = new Bundle();
        args.putString(ARTIST_NAME, artistName);
        args.putString(ARTIST_URL, artistUrl);
        args.putString(ARTIST_ID, artistId);
        ArtistFragment fragment = new ArtistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ArtistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_start, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        mRecyclerViewTopTrack = (RecyclerView) rootView.findViewById(R.id.recycler_view_top_tracks);
        mRecyclerViewTopTrack.setHasFixedSize(true);

        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        mRecyclerViewTopTrack.setLayoutManager(manager);

        mHeaderList = LayoutInflater.from(getActivity()).inflate(R.layout.header_list, mRecyclerViewTopTrack, false);

        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ((ArtistPageTopTrackAdapter) mRecyclerViewTopTrack.getAdapter()).isHeader(position) ?
                        manager.getSpanCount() : 1;
            }
        });

        mRecyclerViewTopTrack.setLayoutManager(new LinearLayoutManager(getActivity()));

        mErrorResultView = rootView.findViewById(R.id.no_result_view);
        TextView textNoResut = (TextView) mErrorResultView.findViewById(R.id.textview_no_result);
        textNoResut.setText(getString(R.string.no_top_tracks_found_text));
        mErrorResultView.setVisibility(View.INVISIBLE);

        mCollapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        if (mCollapsingToolbar == null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
        mImageView = (ImageView) rootView.findViewById(R.id.backdrop);

        Bundle args = getArguments();
        if (args != null) {
            String artistName = args.getString(ARTIST_NAME);
            if (!mTwoPane) {
                mCollapsingToolbar.setTitle(artistName);
            } else {
                ((TextView) rootView.findViewById(R.id.artist_name_header)).setText(artistName);
            }
            String artistUrl = args.getString(ARTIST_URL);
            if (artistUrl == null) {
                mImageView.setImageResource(R.drawable.ic_empty_artist);
            } else {
                Glide.with(this)
                        .load(artistUrl)
                        .placeholder(R.drawable.ic_empty_artist)
                        .centerCrop()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageView);
            }
            mArtistId = args.getString(ARTIST_ID);
        }


        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager
                .registerReceiver(mReceiver, new IntentFilter(MusicService.MEDIA_PLAYER_NEW_TRACK));
        GetTopTrackTask topTrackTask = new GetTopTrackTask();
        topTrackTask.execute(mArtistId);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_artist, menu);
        menu.findItem(R.id.action_playing).setVisible(!mTwoPane);
        menu.findItem(R.id.action_settings).setVisible(!mTwoPane);

        mMenuShare = menu.findItem(R.id.action_share);
        provider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuShare);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        if (id == R.id.action_playing) {
            if (MusicService.getInstance() != null) {
                ArrayList<TrackData> trackDatas = MusicService.getInstance().getTracks();
                int currentPosition = MusicService.getInstance().getPosition();

                if (trackDatas != null && trackDatas.size() > 0) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    MediaPlayerDialogFragment player = MediaPlayerDialogFragment.newInstance(trackDatas,
                            currentPosition);

                    player.show(fm, MediaPlayerDialogFragment.TAG_MEDIA_PLAYER);

                    return true;
                } else {
                /*
                new MaterialDialog.Builder(this)
                        .title(R.string.no_music_title)
                        .content(R.string.no_music_message)
                        .positiveText(R.string.OK)
                        .show();
                        */
                }
            } else {
                Toast.makeText(getActivity(), "No music playing", Toast.LENGTH_LONG).show();
            }

        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshAdapter(List<TrackData> tracks, ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler handler) {
        if (mRecyclerViewTopTrack.getAdapter() == null) {
            ((TextView) mHeaderList).setText(R.string.header_text_top_tracks);
            ArtistPageTopTrackAdapter adapter = new ArtistPageTopTrackAdapter(
                    getActivity(),
                    tracks,
                    mHeaderList,
                    handler);
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

    private class GetTopTrackTask extends AsyncTask<String, Integer, List<TrackData>> {

        @Override
        protected List<TrackData> doInBackground(String... params) {
            Map<String, Object> options = new HashMap<>(1);
            String countryCode = PreferenceHelper.getCountryPreference(getActivity());
            options.put("country", countryCode);
            final List<TrackData> trackDatas = new ArrayList<>();
            SpotifySingleton.getInstance().getService().getArtistTopTrack(params[0], options, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, Response response) {
                    if (response.getReason().equals("OK")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (Track track : tracks.tracks) {
                                    TrackData trackData = new TrackData();
                                    trackData.setId(track.id);
                                    trackData.setName(track.name);
                                    List<ArtistSimple> artistSimples = track.artists;
                                    if (!artistSimples.isEmpty()) {
                                        trackData.setArtistName(artistSimples.get(0).name);
                                    }

                                    trackData.setAlbumName(track.album.name);
                                    trackData.setDuration(track.duration_ms);
                                    trackData.setPreviewUrl(track.preview_url);
                                    List<Image> images = track.album.images;
                                    if (!images.isEmpty()) {
                                        trackData.setUrlAlbum(images.get(0).url);
                                    } else {
                                        trackData.setUrlAlbum(null);
                                    }
                                    trackDatas.add(trackData);
                                }
                                mTracks.addAll(trackDatas);
                                refreshAdapter(trackDatas, mHandler);
                            }
                        });

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), response.getReason(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("FAILED", error.getResponse().getReason());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTracks.clear();
                            refreshAdapter(null, mHandler);
                        }
                    });
                }
            });
            return trackDatas;
        }

        @Override
        protected void onPostExecute(List<TrackData> trackDatas) {

        }
    }

    private void createShareIntent() {
        if (MusicService.getInstance() != null) {
            TrackData trackData = MusicService.getInstance().getCurrentTrack();
            if (trackData != null) {
                String url = trackData.getPreviewUrl();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                shareIntent.setType("text/html");
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                if (provider != null) {
                    provider.setShareIntent(shareIntent);
                }
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.MEDIA_PLAYER_NEW_TRACK.equals(intent.getAction())) {
                createShareIntent();
            }
        }
    };
}
