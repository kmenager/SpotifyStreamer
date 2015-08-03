package io.github.kmenager.spotifystreamer.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.activities.ArtistActivity;
import io.github.kmenager.spotifystreamer.activities.SearchActivity;
import io.github.kmenager.spotifystreamer.adapters.SearchArtistAdapter;
import io.github.kmenager.spotifystreamer.model.ArtistData;


public class SearchArtistFragment extends Fragment {


    private static final String LIST_ARTIST = "LIST_ARTIST";
    private static final String KEYWORD = "KEYWORD";
    private String mKeyword;

    private ArrayList<ArtistData> mArtists = new ArrayList<>();


    private View mHeaderList;
    private RecyclerView mRecyclerViewArtist;
    private View mEmptyView;
    private ProgressBar mProgressBar;
    private View mErrorResultView;
    private boolean mTwoPane;



    public static SearchArtistFragment newInstance() {
        return new SearchArtistFragment();
    }

    private SearchArtistAdapter.SearchArtistAdapterOnClickHandler mHandler =
            new SearchArtistAdapter.SearchArtistAdapterOnClickHandler() {
                @Override
                public void onClick(ArtistData artistData, SearchArtistAdapter.SearchArtistAdapterHolder vh) {
                    if (mTwoPane) {
                        Bundle args = new Bundle();
                        ArtistFragment fragment = ArtistFragment.newInstance(
                                artistData.getArtistId(),
                                artistData.getName(),
                                artistData.getUrlImage());

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, fragment, ArtistActivity.TAG_ARTIST_FRAGMENT)
                                .commit();
                    } else {
                        Intent intent = new Intent(getActivity(), ArtistActivity.class);
                        intent.putExtra(SearchActivity.ARTIST_INTENT, artistData);

                        ActivityOptionsCompat activityOptions =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                        new Pair<View, String>(vh.mImageViewIcon, getString(R.string.transition_image_key)));
                        ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
                    }
                }
            };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);
        if (rootView.findViewById(R.id.main_content) != null) {
            mTwoPane = false;
        } else {
            mTwoPane = true;
        }
        mErrorResultView = rootView.findViewById(R.id.no_result_view);
        TextView textNoResut = (TextView) mErrorResultView.findViewById(R.id.textview_no_result);
        textNoResut.setText(getString(R.string.no_result_found_text));
        mErrorResultView.setVisibility(View.INVISIBLE);
        mRecyclerViewArtist = (RecyclerView) rootView.findViewById(R.id.recycler_view_search);
        mRecyclerViewArtist.setHasFixedSize(true);

        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        mRecyclerViewArtist.setLayoutManager(manager);

        mHeaderList = LayoutInflater.from(getActivity()).inflate(R.layout.header_list, mRecyclerViewArtist, false);

        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ((SearchArtistAdapter)mRecyclerViewArtist.getAdapter()).isHeader(position) ? manager.getSpanCount() : 1;
            }
        });

        mRecyclerViewArtist.setLayoutManager(new LinearLayoutManager(getActivity()));

        mEmptyView = rootView.findViewById(R.id.recyclerview_search_empty);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar_search);
        showProgressBar(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mKeyword = savedInstanceState.getString(KEYWORD);
            mArtists = savedInstanceState.getParcelableArrayList(LIST_ARTIST);
            refreshAdapterArtists(mArtists);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LIST_ARTIST, mArtists);
        outState.putString(KEYWORD, mKeyword);
    }

    public void refreshAdapterArtists(List<ArtistData> artists) {
        if (mRecyclerViewArtist.getAdapter() == null) {
            ((TextView)mHeaderList).setText(R.string.header_text_search_artist);
            SearchArtistAdapter adapter = new SearchArtistAdapter(
                    getActivity(),
                    artists,
                    mHeaderList,
                    mHandler
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
            mArtists.addAll(artists);
        }
    }

    public void showEmptyView(boolean state) {
        mEmptyView.setVisibility(state ? View.VISIBLE : View.GONE);

    }

    public void showProgressBar(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void showErrorResultView(boolean visible) {
        mErrorResultView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
