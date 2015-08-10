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
    private ArtistFragment mArtistFragment;



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
        mArtistFragment = (ArtistFragment) fm.findFragmentById(R.id.fragment);
        if (mArtistFragment == null) {
            mArtistFragment = ArtistFragment.newInstance(artistId, artistName, artistUrl);

            fm.beginTransaction()
                    .add(R.id.fragment, mArtistFragment)
                    .commit();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_artist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }
    */
}
