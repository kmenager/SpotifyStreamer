package io.github.kmenager.spotifystreamer;


import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class SpotifySingleton {

    private static SpotifyApi mApi;

    public static SpotifyApi getInstance() {
        if (mApi == null) {
            mApi = new SpotifyApi();
        }
        return mApi;
    }

    public SpotifyService getService() {
        return mApi.getService();
    }
}
