package io.github.kmenager.spotifystreamer.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.SpotifySingleton;
import io.github.kmenager.spotifystreamer.adapters.ArtistPageTopTrackAdapter;
import io.github.kmenager.spotifystreamer.model.TrackData;
import io.github.kmenager.spotifystreamer.utils.NetworkHelper;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

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

    private MediaPlayerDialogFragment mPlayer;


    private ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler mHandler =
            new ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterOnClickHandler() {
                @Override
                public void onClick(TrackData trackData, ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterHolder vh) {
                    if (NetworkHelper.isOnline(getActivity())) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();

                        mPlayer = MediaPlayerDialogFragment.newInstance(mTracks, vh.getLayoutPosition() - 1);
                        mPlayer.show(fm, MediaPlayerDialogFragment.TAG_MEDIA_PLAYER);
                        /*Intent audioService = new Intent(ArtistActivity.this, AudioPlayerService.class);
                        getApplicationContext().startService(audioService);*/
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
    public void onResume() {
        super.onResume();
        GetTopTrackTask topTrackTask = new GetTopTrackTask();
        topTrackTask.execute(mArtistId);
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
            String locale = getResources().getConfiguration().locale.getISO3Country();
            options.put("country", locale.substring(0, 2));
            List<TrackData> trackDatas = new ArrayList<>();
            List<Track> tracks = SpotifySingleton.getInstance().getService().getArtistTopTrack(params[0], options).tracks;
            for (Track track : tracks) {
                TrackData trackData = new TrackData();
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
            return trackDatas;
        }



        @Override
        protected void onPostExecute(List<TrackData> tracks) {
            mTracks.addAll(tracks);
            refreshAdapter(tracks, mHandler);
        }
    }
}
