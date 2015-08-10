package io.github.kmenager.spotifystreamer.activities;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import io.github.kmenager.spotifystreamer.SpotifySingleton;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import kaaes.spotify.webapi.android.models.Artist;

public class SpotifyRequest extends Fragment {

    public interface SpotifyRequestCallback<T> {
        void onPreExecute();

        void onCancelled();

        void onPostExecute(List<T> lists);
    }

    private SpotifyRequestCallback mCallbacks;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (SpotifyRequestCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void getArtists(String keyword) {
        GetArtistsTask artistsTask = new GetArtistsTask();
        artistsTask.execute(keyword);
    }

    private class GetArtistsTask extends AsyncTask<String, Integer, List<ArtistData>> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        @Override
        protected List<ArtistData> doInBackground(String... params) {
            List<ArtistData> artistDatas = new ArrayList<>();
            List<Artist> artists = SpotifySingleton.getInstance().getService().searchArtists(params[0] + "*").artists.items;
            for (Artist artist : artists) {
                ArtistData artistData = new ArtistData();
                artistData.setArtistId(artist.id);
                artistData.setName(artist.name);
                if (!artist.images.isEmpty()) {
                    artistData.setUrlImage(artist.images.get(0).url);
                } else {
                    artistData.setUrlImage(null);
                }
                artistDatas.add(artistData);
            }

            return artistDatas;
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<ArtistData> artists) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(artists);
            }
        }
    }
}
