package io.github.kmenager.spotifystreamer.model;


import android.os.Parcel;
import android.os.Parcelable;

public class TrackData implements Parcelable {

    private String mName;
    private String mPreviewUrl;
    private long mDuration; // ms
    private String mUrlAlbum;
    private String mArtistName;
    private String mAlbumName;

    public TrackData() {}

    protected TrackData(Parcel in) {
        mName = in.readString();
        mPreviewUrl = in.readString();
        mDuration = in.readLong();
        mUrlAlbum = in.readString();
        mArtistName = in.readString();
        mAlbumName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPreviewUrl);
        dest.writeLong(mDuration);
        dest.writeString(mUrlAlbum);
        dest.writeString(mArtistName);
        dest.writeString(mAlbumName);
    }

    public static final Creator<TrackData> CREATOR = new Creator<TrackData>() {
        @Override
        public TrackData createFromParcel(Parcel in) {
            return new TrackData(in);
        }

        @Override
        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        mPreviewUrl = previewUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getUrlAlbum() {
        return mUrlAlbum;
    }

    public void setUrlAlbum(String urlAlbum) {
        mUrlAlbum = urlAlbum;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }
}
