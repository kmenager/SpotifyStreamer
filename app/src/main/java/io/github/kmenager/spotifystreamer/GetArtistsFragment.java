package io.github.kmenager.spotifystreamer;


import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;

public class GetArtistsFragment extends Fragment {

    interface GetArtistsCallbacks {
        void onPreExecute();
        void onCancelled();
        void onPostExecute(List<Artist> artists);
    }

    private GetArtistsCallbacks mCallbacks;
    private GetArtistsTask mArtistsTask;

    private SpotifyApi api = new SpotifyApi();
    private SpotifyService spotifyService = api.getService();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void getArtists(String keyword) {
        mArtistsTask = new GetArtistsTask();
        mArtistsTask.execute(keyword);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (GetArtistsCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private class GetArtistsTask extends AsyncTask<String, Integer, List<Artist>>{

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        @Override
        protected List<Artist> doInBackground(String... params) {
            return spotifyService.searchArtists(params[0]).artists.items;
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(artists);
            }
        }
    }
}
