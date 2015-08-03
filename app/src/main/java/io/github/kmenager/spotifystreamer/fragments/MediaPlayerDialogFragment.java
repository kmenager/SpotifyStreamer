package io.github.kmenager.spotifystreamer.fragments;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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

import io.github.kmenager.spotifystreamer.AudioPlayerService;
import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.model.TrackData;
import io.github.kmenager.spotifystreamer.utils.NetworkHelper;

public class MediaPlayerDialogFragment extends DialogFragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    public static final String TAG_MEDIA_PLAYER = "MEDIA_PLAYER_FRAGMENT";

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

    private AudioPlayerService mAudioPlayerService;
    private TrackData mTrackData;
    private ArrayList<TrackData> mTrackDatas;
    private boolean mIsPause;
    private long mTimeElapsed;
    private static final long MAX_DURATION_30_SEC = 30 * 1000;

    private Handler mHandler = new Handler();
    private int mCurrentPosition;
    private int mSeekTo = -1;

    public static MediaPlayerDialogFragment newInstance(ArrayList<TrackData> tracks, int layoutPosition) {

        MediaPlayerDialogFragment mediaPlayerDialogFragment = new MediaPlayerDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("listtrack", tracks);
        args.putInt("position", layoutPosition);
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
            mTrackDatas = getArguments().getParcelableArrayList("listtrack");
            mCurrentPosition = getArguments().getInt("position");
            mTrackData = mTrackDatas.get(mCurrentPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onAudioStart(-1);

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

        mAudioPlayerService = new AudioPlayerService();

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
        onUpdateView();
        return rootView;
    }

    private void onUpdateView() {
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

        mCurrentTime.setText(new SimpleDateFormat("m:ss", Locale.getDefault()).format(0));

        mDuration.setText("0:30");//new SimpleDateFormat("m:s", Locale.getDefault()).format(mTrackData.getDuration()));

        onUpdateButton();

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

        if (mAudioPlayerService != null) {
            mPlay.setImageDrawable(mPauseDrawable);
            mAudioPlayerService.getPlayer().start();
            mIsPause = false;
        }
    }

    private void onAudioPause() {
        if (mAudioPlayerService != null) {
            mPlay.setImageDrawable(mPlayDrawable);
            mAudioPlayerService.onPauseAudio();
            mIsPause = true;
        }
    }

    private void onAudioStart(int seekTo) {
        mPlay.setImageDrawable(mPauseDrawable);
        mAudioPlayerService.onPlayAudio(getActivity(), mTrackData.getPreviewUrl(), seekTo);
        mAudioPlayerService.getPlayer().setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mSeekTo = -1;
                        if ((mCurrentPosition + 1) < mTrackDatas.size()) {
                            mCurrentPosition++;
                            mTrackData = mTrackDatas.get(mCurrentPosition);
                            if (isVisible())
                                onUpdateView();

                            onAudioStart(mSeekTo);
                        } else {
                            onAudioStop();
                        }
                    }
                }
        );
        updateSeekBar();
    }

    public void onAudioStop() {
        mPlay.setImageDrawable(mPlayDrawable);

        if (mIsPause) {
            mIsPause = false;
        } else {
            mHandler.removeCallbacks(updateSeekBarTask);
            mAudioPlayerService.onStopAudio();
            mSeekBar.setProgress(0);
            mCurrentTime.setText(
                    new SimpleDateFormat("m:ss", Locale.getDefault()).format(0));
        }
    }

    private void updateSeekBar() {
        mHandler.postDelayed(updateSeekBarTask, 100);
    }

    private Runnable updateSeekBarTask = new Runnable() {
        @Override
        public void run() {
            mTimeElapsed = mAudioPlayerService.getCurrentPosition();
            mSeekBar.setProgress((int) mTimeElapsed);
            mCurrentTime.setText(
                    new SimpleDateFormat("m:ss", Locale.getDefault()).format(mTimeElapsed));
            mHandler.postDelayed(this, 100);
        }
    };

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
                    boolean shouldPlay = false;
                    if (mAudioPlayerService.isPlaying() || mIsPause) {
                        onAudioStop();
                        shouldPlay = true;
                    }

                    if ((mCurrentPosition + 1) < mTrackDatas.size()) {

                        mCurrentPosition++;
                        mTrackData = mTrackDatas.get(mCurrentPosition);
                        onUpdateView();
                        if (shouldPlay) {
                            onAudioStart(-1);
                        }
                    }

                }
                break;
            case R.id.playPause:
                if (NetworkHelper.isOnline(getActivity())) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mAudioPlayerService.isPlaying()) {
                                if (!mIsPause) {
                                    onAudioStart(mSeekTo);
                                } else {
                                    onAudioResume();
                                }
                            } else {
                                onAudioPause();
                            }
                        }
                    });

                }
                break;
            case R.id.previous:
                if (NetworkHelper.isOnline(getActivity())) {
                    boolean shouldPlay = false;
                    if (mAudioPlayerService.isPlaying() || mIsPause) {
                        onAudioStop();
                        shouldPlay = true;
                    }

                    if ((mCurrentPosition - 1) >= 0) {
                        mCurrentPosition--;
                        mTrackData = mTrackDatas.get(mCurrentPosition);
                        onUpdateView();
                        final boolean finalShouldPlay = shouldPlay;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalShouldPlay) {
                                    onAudioStart(-1);
                                }
                            }
                        });
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
        mHandler.removeCallbacks(updateSeekBarTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(updateSeekBarTask);
        mSeekTo = seekBar.getProgress();
        if (mAudioPlayerService.getPlayer() != null) {
            mAudioPlayerService.getPlayer().seekTo(seekBar.getProgress());
            updateSeekBar();
        } else {
            mCurrentTime.setText(
                    new SimpleDateFormat("m:ss", Locale.getDefault()).format(seekBar.getProgress()));
        }
    }
}
