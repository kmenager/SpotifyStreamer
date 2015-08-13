package io.github.kmenager.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.github.kmenager.spotifystreamer.MusicService;
import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.model.ArtistData;
import io.github.kmenager.spotifystreamer.model.TrackData;
import io.github.kmenager.spotifystreamer.utils.NetworkHelper;

public class MediaPlayerDialogFragment extends DialogFragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, ServiceConnection {

    public static final String TAG_MEDIA_PLAYER = "MEDIA_PLAYER_FRAGMENT";
    private static final String ARGS_LIST_TRACKS = "ARGS_LIST_TRACKS";
    private static final String ARGS_CURRENT_POSITION = "ARGS_CURRENT_POSITION";
    private static final String ARGS_ARTIST_DATA = "ARGS_ARTIST_DATA";
    private static final String ARGS_SEEK_POSITION = "ARGS_SEEK_POSITION";

    private TextView mArtistName;
    private TextView mAlbumName;
    private ImageView mAlbumArtwork;
    private TextView mTrackName;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mDuration;
    private ImageButton mPrevious;
    private ImageButton mPlay;
    private ImageButton mNext;

    private Drawable mPlayDrawable;
    private Drawable mPauseDrawable;

    private MusicService mMusicService;
    private TrackData mTrackData;
    private ArtistData mArtistData;
    private ArrayList<TrackData> mTrackDatas;
    private boolean mIsPause;
    private static final long MAX_DURATION_30_SEC = 30 * 1000;

    private int mCurrentPosition;
    LocalBroadcastManager mLocalBroadcastManager;
    private boolean mIsAlreadyUpdate;

    public static MediaPlayerDialogFragment newInstance(ArrayList<TrackData> tracks, int layoutPosition, ArtistData artistData) {

        MediaPlayerDialogFragment mediaPlayerDialogFragment = new MediaPlayerDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_LIST_TRACKS, tracks);
        args.putInt(ARGS_CURRENT_POSITION, layoutPosition);
        args.putParcelable(ARGS_ARTIST_DATA, artistData);
        mediaPlayerDialogFragment.setArguments(args);
        return mediaPlayerDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsPause = false;
        mTrackData = null;
        Bundle args = getArguments();
        if (args != null) {
            mTrackDatas = getArguments().getParcelableArrayList(ARGS_LIST_TRACKS);
            mCurrentPosition = getArguments().getInt(ARGS_CURRENT_POSITION);
            mTrackData = mTrackDatas.get(mCurrentPosition);
            mArtistData = getArguments().getParcelable(ARGS_ARTIST_DATA);
        }

        setRetainInstance(true);
        Intent bindIntent = new Intent(getActivity().getApplicationContext(), MusicService.class);
        getActivity().getApplicationContext().startService(bindIntent);
        getActivity().getApplicationContext().bindService(bindIntent, this, Context.BIND_AUTO_CREATE);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARGS_LIST_TRACKS, mTrackDatas);
        outState.putInt(ARGS_CURRENT_POSITION, mCurrentPosition);
        outState.putParcelable(ARGS_ARTIST_DATA, mArtistData);
        outState.putInt(ARGS_SEEK_POSITION, mSeekBar.getProgress());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        mLocalBroadcastManager
                .registerReceiver(mReceiver, new IntentFilter(MusicService.MEDIA_PLAYER_STATUS));
        mLocalBroadcastManager
                .registerReceiver(mReceiver, new IntentFilter(MusicService.MEDIA_PLAYER_NEW_TRACK));

        super.onResume();
    }

    @Override
    public void onPause() {
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMusicService != null) {
            getActivity().getApplicationContext().unbindService(this);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_player, container, false);

        mPlayDrawable = getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null);
        if (mPlayDrawable != null) {
            mPlayDrawable = DrawableCompat.wrap(mPlayDrawable);
            DrawableCompat.setTint(mPlayDrawable, getResources().getColor(R.color.accent));
            DrawableCompat.setTintMode(mPlayDrawable, PorterDuff.Mode.SRC_ATOP);
        }

        mPauseDrawable = getResources().getDrawable(R.drawable.ic_pause_circle_filled_black_48dp, null);
        if (mPauseDrawable != null) {
            mPauseDrawable = DrawableCompat.wrap(mPauseDrawable);
            DrawableCompat.setTint(mPauseDrawable, getResources().getColor(R.color.accent));
            DrawableCompat.setTintMode(mPauseDrawable, PorterDuff.Mode.SRC_ATOP);
        }

        mArtistName = (TextView) rootView.findViewById(R.id.artistName);

        mAlbumName = (TextView) rootView.findViewById(R.id.albumName);

        mAlbumArtwork = (ImageView) rootView.findViewById(R.id.albumArtwork);

        mTrackName = (TextView) rootView.findViewById(R.id.trackName);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mCurrentTime = (TextView) rootView.findViewById(R.id.trackCurrent);

        mDuration = (TextView) rootView.findViewById(R.id.trackDuration);

        mPrevious = (ImageButton) rootView.findViewById(R.id.previous);
        mPrevious.setOnClickListener(this);

        mPlay = (ImageButton) rootView.findViewById(R.id.playPause);
        mPlay.setImageDrawable(mPlayDrawable);
        mPlay.setOnClickListener(this);

        mNext = (ImageButton) rootView.findViewById(R.id.next);
        mNext.setOnClickListener(this);
        onUpdateView(savedInstanceState);
        return rootView;
    }


    private void onUpdateView(Bundle savedInstanceState) {
        if (!mIsAlreadyUpdate) {
            mArtistName.setText(mTrackData.getArtistName());
            mAlbumName.setText(mTrackData.getAlbumName());

            if (!mTrackData.getUrlAlbum().isEmpty()) {
                Glide.with(getActivity())
                        .load(mTrackData.getUrlAlbum())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(mAlbumArtwork);
            } else {
                mAlbumArtwork.setImageResource(R.drawable.ic_album_empty);
            }

            mTrackName.setText(mTrackData.getName());

            mSeekBar.setMax((int) MAX_DURATION_30_SEC); //mTrackData.getDuration() for normal usage

            mCurrentTime.setText(new SimpleDateFormat(getActivity().getString(R.string.format_time),
                    Locale.getDefault()).format(0));

            mDuration.setText(R.string.default_thirty_seconds);//new SimpleDateFormat("m:s", Locale.getDefault()).format(mTrackData.getDuration()));

            onUpdateButton();
        }

        if (savedInstanceState != null) {
            mCurrentTime.setText(new SimpleDateFormat(getActivity().getString(R.string.format_time),
                    Locale.getDefault()).format(savedInstanceState.getInt(ARGS_SEEK_POSITION)));
        }

    }

    private void onUpdateButton() {
        if (mCurrentPosition == 0) {
            mPrevious.setVisibility(View.INVISIBLE);
            mNext.setVisibility(View.VISIBLE);
        } else if (mCurrentPosition < mTrackDatas.size() - 1) {
            mPrevious.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
        } else if (mCurrentPosition >= mTrackDatas.size() - 1) {
            mPrevious.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.INVISIBLE);
        }
    }

    private void onAudioResume() {
        mPlay.setImageDrawable(mPauseDrawable);
        mMusicService.onResumeAudio();
        mIsPause = false;
    }

    private void onAudioPause() {
        mPlay.setImageDrawable(mPlayDrawable);
        mMusicService.onPauseAudio();
        mIsPause = true;
    }

    private void onAudioStart() {
        mPlay.setImageDrawable(mPauseDrawable);
        mMusicService.onPlayTrack();
    }

    public void togglePlayPause() {
        if (mMusicService != null) {
            if (mMusicService.isPlaying()) {
                onAudioPause();
            } else {
                if (mIsPause) {
                    onAudioResume();
                } else {
                    onAudioStart();
                }
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                if (NetworkHelper.isOnline(getActivity())) {
                    if (mMusicService != null) {
                        mMusicService.onPlayNext();
                        onUpdateView(null);
                    }
                }
                break;
            case R.id.playPause:
                if (NetworkHelper.isOnline(getActivity())) {
                    togglePlayPause();
                }
                break;
            case R.id.previous:
                if (NetworkHelper.isOnline(getActivity())) {
                    if (mMusicService != null) {
                        mMusicService.onPlayPrev();
                    }
                }
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mMusicService != null) {
            mMusicService.removeCallback();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMusicService.removeCallback();
        int seekTo = seekBar.getProgress();
        if (mMusicService.getPlayer() != null) {
            mMusicService.getPlayer().seekTo(seekBar.getProgress());
            mMusicService.setCallback();
        } else {
            mCurrentTime.setText(
                    new SimpleDateFormat(getActivity().getString(R.string.format_time),
                            Locale.getDefault()).format(seekBar.getProgress()));
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mMusicService = ((MusicService.LocalBinder) service).getService();
        mMusicService.setTracks(mTrackDatas);
        mMusicService.setPosition(mCurrentPosition);
        mMusicService.setArtistData(mArtistData);
        if (!mMusicService.isPlaying()) {
            if (!mIsPause) {
                onAudioStart();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mMusicService = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MusicService.MEDIA_PLAYER_STATUS)) {
                TrackData trackData = intent.getParcelableExtra(MusicService.TRACK_DATA);
                if (trackData.getId().equals(mTrackData.getId())) {
                    onUpdateView(null);
                    if (mMusicService != null) {
                        long timeElapsed = mMusicService.getCurrentPosition();
                        mSeekBar.setProgress((int) timeElapsed);
                        mCurrentTime.setText(
                                new SimpleDateFormat(context.getString(R.string.format_time),
                                        Locale.getDefault()).format(timeElapsed));
                        mIsAlreadyUpdate = true;
                        if (mMusicService.isPlaying()) {
                            mPlay.setImageDrawable(mPauseDrawable);
                        } else {
                            mPlay.setImageDrawable(mPlayDrawable);
                        }
                    }
                }

            } else if (MusicService.MEDIA_PLAYER_NEW_TRACK.equals(intent.getAction())) {
                mIsAlreadyUpdate = false;
                mTrackData = intent.getParcelableExtra(MusicService.TRACK_DATA);
                mCurrentPosition = intent.getIntExtra(MusicService.TRACK_POSITION, mCurrentPosition);
                onUpdateView(null);
            }
        }
    };
}
