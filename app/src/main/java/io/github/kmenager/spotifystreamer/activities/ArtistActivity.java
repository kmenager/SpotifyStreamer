package io.github.kmenager.spotifystreamer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.fragments.ArtistFragment;
import io.github.kmenager.spotifystreamer.model.ArtistData;

public class ArtistActivity extends AppCompatActivity {

    public static final String TAG_ARTIST_FRAGMENT = "ARTIST_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Intent intent = getIntent();
        ArtistData artistData = intent.getParcelableExtra(SearchActivity.ARTIST_INTENT);

        String artistId = artistData.getArtistId();
        String artistUrl = artistData.getUrlImage();
        String artistName = artistData.getName();

        FragmentManager fm = getSupportFragmentManager();
        ArtistFragment artistFragment = (ArtistFragment) fm.findFragmentById(R.id.fragment);
        if (artistFragment == null) {
            artistFragment = ArtistFragment.newInstance(artistId, artistName, artistUrl);

            fm.beginTransaction()
                    .add(R.id.fragment, artistFragment)
                    .commit();
        }
    }
}
