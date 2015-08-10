package io.github.kmenager.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

import io.github.kmenager.spotifystreamer.activities.SearchActivity;
import io.github.kmenager.spotifystreamer.model.TrackData;
import io.github.kmenager.spotifystreamer.utils.AlbumArtCache;
import io.github.kmenager.spotifystreamer.utils.PreferenceHelper;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final int NOTIFICATION_ID = 1337;
    private static final int REQUEST_CODE = 1007;
    private static final int ELAPSE_DELAY = 100;

    public static final String MEDIA_PLAYER_STATUS = "MEDIA_PLAYER_STATUS";
    public static final String MEDIA_PLAYER_NEW_TRACK = "MEDIA_PLAYER_NEW_TRACK";
    public static final String TRACK_POSITION = "TRACK_POSITION";
    public static final String TRACK_DATA = "TRACK_DATA";

    public static final String ACTION_PAUSE = "io.github.kmenager.spotifystreamer.pause";
    public static final String ACTION_PLAY = "io.github.kmenager.spotifystreamer.play";
    public static final String ACTION_PREV = "io.github.kmenager.spotifystreamer.prev";
    public static final String ACTION_NEXT = "io.github.kmenager.spotifystreamer.next";

    private ArrayList<TrackData> mTracks;
    private Handler mHandler = new Handler();
    private final IBinder mBinder = new LocalBinder();
    private static MediaPlayer mPlayer;
    private int mCurrentPosition;
    private NotificationManager mNotificationManager;
    private LocalBroadcastManager mBroadcastManager;
    private boolean mIsPause;

    private static MusicService mInstance = null;
    public static MusicService getInstance() {
        return mInstance;
    }

    private PendingIntent mPauseIntent;
    private PendingIntent mPlayIntent;
    private PendingIntent mPreviousIntent;
    private PendingIntent mNextIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mIsPause = false;
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mCurrentPosition = 0;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        mPauseIntent = PendingIntent.getService(
                this,
                REQUEST_CODE,
                new Intent(this, MusicService.class).setAction(ACTION_PAUSE),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mPlayIntent = PendingIntent.getService(
                this,
                REQUEST_CODE,
                new Intent(this, MusicService.class).setAction(ACTION_PLAY),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mPreviousIntent = PendingIntent.getService(
                this,
                REQUEST_CODE,
                new Intent(this, MusicService.class).setAction(ACTION_PREV),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mNextIntent = PendingIntent.getService(
                this,
                REQUEST_CODE,
                new Intent(this, MusicService.class).setAction(ACTION_NEXT),
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        onPlayTrack();
                        break;
                    case ACTION_PAUSE:
                        onPauseAudio();
                        break;
                    case ACTION_PREV:
                        onPlayPrev();
                        break;
                    case ACTION_NEXT:
                        onPlayNext();
                        break;
                    default:
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mInstance = null;
        stopForeground(true);
        mTracks = null;
        onAudioStop();
        mNotificationManager.cancelAll();
        super.onDestroy();
    }

    /**
     * True if media player is playing, false otherwise
     *
     * @return the status of media player
     */
    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    /**
     * Resume Media player
     */
    public void onResumeAudio() {
        if (mPlayer != null) {
            mPlayer.start();
            mIsPause = false;
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    /**
     * Pause Media player
     */
    public void onPauseAudio() {
        if (mPlayer != null) {
            mPlayer.pause();
            mIsPause = true;
            mNotificationManager.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    /**
     * Play previous if exist or the first one
     */
    public void onPlayPrev() {
        if (mCurrentPosition > 0) {
            mCurrentPosition--;
        } else {
            mCurrentPosition = mTracks.size() - 1;
        }
        onPlayTrack();
    }

    /**
     * Play next if exist or start from beginning
     */
    public void onPlayNext() {
        if (mCurrentPosition < mTracks.size() - 1) {
            mCurrentPosition++;
        } else {
            mCurrentPosition = 0;
        }
        onPlayTrack();
    }

    /**
     * Play track from mCurrentPosition
     */
    public void onPlayTrack() {
        if (mTracks != null) {
            if (mPlayer != null) {
                if (mIsPause) {
                    onResumeAudio();
                } else {
                    mPlayer.reset();
                    mHandler.removeCallbacks(updateTrack);
                    loadAndPlay(mTracks.get(mCurrentPosition));
                }

            }
        }
    }

    /**
     * Get current time elapse from media player
     *
     * @return elapse time
     */
    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    /**
     * Get the current media player
     *
     * @return the media player
     */
    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    /**
     * Set current position in track list
     *
     * @param position
     */
    public void setPosition(int position) {
        mCurrentPosition = position;
    }

    /**
     * Set list of tracks
     *
     * @param tracks
     */
    public void setTracks(ArrayList<TrackData> tracks) {
        mTracks = tracks;
    }

    /**
     * Prepare media player with current track
     *
     * @param track
     */
    public void loadAndPlay(TrackData track) {
        try {
            mPlayer.setDataSource(track.getPreviewUrl());
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
    }

    /**
     * Release media player
     */
    public void onAudioStop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public IBinder onBind(@Nullable Intent intent) {
        return mBinder;
    }

    public ArrayList<TrackData> getTracks() {
        return mTracks;
    }

    public int getPosition() {
        return mCurrentPosition;
    }

    public TrackData getCurrentTrack() {
        if (mTracks != null) {
            return mTracks.get(mCurrentPosition);
        } else {
            return null;
        }
    }

    /**
     * Local binder to get this service
     */
    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mHandler.postDelayed(updateTrack, ELAPSE_DELAY);
        Intent intent = new Intent(MEDIA_PLAYER_NEW_TRACK);
        intent.putExtra(TRACK_DATA, mTracks.get(mCurrentPosition));
        intent.putExtra(TRACK_POSITION, mCurrentPosition);
        mBroadcastManager.sendBroadcast(intent);
        Notification notification = buildNotification();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeCallbacks(updateTrack);
        if (mCurrentPosition + 1 >= mTracks.size()) {
            onAudioStop();
            mNotificationManager.cancel(NOTIFICATION_ID);
        } else {
            onPlayNext();
            Notification notification = buildNotification();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }

    }

    private Runnable updateTrack = new Runnable() {
        public void run() {
            if (mPlayer != null) {
                // Broadcast current playing status (STARTED)
                // Will let control UI to update progress and time
                Intent intent = new Intent(MEDIA_PLAYER_STATUS);
                intent.putExtra(TRACK_DATA, mTracks.get(mCurrentPosition));
                mBroadcastManager.sendBroadcast(intent);
                mHandler.postDelayed(this, 100);
            }
        }
    };

    public void removeCallback() {
        mHandler.removeCallbacks(updateTrack);
    }

    public void setCallback() {
        mHandler.postDelayed(updateTrack, ELAPSE_DELAY);
    }

    /**
     * Build notification for Music service
     *
     * @return notification with current track data
     */
    private Notification buildNotification() {
        Notification.Builder notificationBuilder = new Notification.Builder(this);

        notificationBuilder.addAction(R.drawable.ic_skip_previous_black_36dp,
                "Previous", mPreviousIntent);

        addPlayPauseAction(notificationBuilder);

        notificationBuilder.addAction(R.drawable.ic_skip_next_black_36dp,
                "Next", mNextIntent);


        TrackData trackData = mTracks.get(mCurrentPosition);
        String fetchUrl = null;
        Bitmap art = null;
        if (trackData.getUrlAlbum() != null) {
            art = AlbumArtCache.getInstance().getBigImage(trackData.getUrlAlbum());
            if (art == null) {
                fetchUrl = trackData.getUrlAlbum();
                art = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_album_empty);
            }
        }


        notificationBuilder.setStyle(
                new Notification.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{1}))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setVisibility(PreferenceHelper.getNotificationVisibility(this))
                .setContentIntent(createContentIntent())
                .setContentTitle(mTracks.get(mCurrentPosition).getArtistName())
                .setContentText(mTracks.get(mCurrentPosition).getName())
                .setLargeIcon(art);

        // Make sure that the notification can be dismissed by the user when we are not playing:
        boolean isplay = mPlayer.isPlaying();
        notificationBuilder.setOngoing(false);
        if (fetchUrl != null) {
            fetchBitmapFromURLAsync(fetchUrl, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    /**
     * Create intent when notification is selected
     *
     * @return intent
     */
    private PendingIntent createContentIntent() {
        return PendingIntent.getActivity(getApplicationContext(),
                REQUEST_CODE,
                new Intent(getApplicationContext(), SearchActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Add play or pause button action
     *
     * @param builder
     */
    private void addPlayPauseAction(Notification.Builder builder) {
        String label;
        int icon;
        PendingIntent intent;
        if (mPlayer.isPlaying()) {
            label = "Pause";
            icon = R.drawable.ic_pause_white_36dp;
            intent = mPauseIntent;
        } else {
            label = "Play";
            icon = R.drawable.ic_play_arrow_white_36dp;
            intent = mPlayIntent;
        }
        builder.addAction(new Notification.Action(icon, label, intent));
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final Notification.Builder builder) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                TrackData trackData = mTracks.get(mCurrentPosition);
                if (trackData != null && trackData.getUrlAlbum().equals(artUrl)) {
                    builder.setLargeIcon(bitmap);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }
}
