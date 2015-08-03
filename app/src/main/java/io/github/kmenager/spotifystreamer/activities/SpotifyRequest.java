package io.github.kmenager.spotifystreamer.activities;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.kmenager.spotifystreamer.SpotifySingleton;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import io.github.kmenager.spotifystreamer.model.TrackData;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

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

    public void getTopTracks(String keyword) {
        GetTopTrackTask topTrackTask = new GetTopTrackTask();
        topTrackTask.execute(keyword);
    }

    private class GetTopTrackTask extends AsyncTask<String, Integer, List<TrackData>> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

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
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(List<TrackData> tracks) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(tracks);
            }
        }
    }
}
