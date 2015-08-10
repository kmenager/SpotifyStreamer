package io.github.kmenager.spotifystreamer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

public class AudioPlayerService extends IntentService implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    private static final String TAG = AudioPlayerService.class.getSimpleName();

    public static final String ACTION_STOP="ACTION_STOP";
    public static final String ACTION_PLAY="ACTION_PLAY";
    public static final String ACTION_PAUSE="ACTION_PAUSE";




    private static MediaPlayer mPlayer;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public AudioPlayerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!action.isEmpty()) {
                switch (action) {
                    case ACTION_PLAY:
                        //onPlayAudio(mTrackData.getPreviewUrl());
                        break;
                    case ACTION_PAUSE:
                        onPauseAudio();
                        break;
                    case ACTION_STOP:
                        onStopAudio();
                        break;
                    default:
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onDestroy();
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    /**
     * Stop Media player
     */
    public void onStopAudio() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * Pause Media player
     */
    public void onPauseAudio() {
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        } else {
            return -1;
        }
    }
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public void onPlayAudio(Context context, String previewUrl, int seekTo) {
        Uri mUri = Uri.parse(previewUrl);
        mPlayer = MediaPlayer.create(context, mUri);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        if (seekTo != -1) {
            mPlayer.seekTo(seekTo);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
