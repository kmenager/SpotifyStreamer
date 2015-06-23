package io.github.kmenager.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ArtistData implements Parcelable{

    private String mArtistId;
    private String mName;
    private String mUrlImage;

    public ArtistData() {}

    protected ArtistData(Parcel in) {
        mArtistId = in.readString();
        mName = in.readString();
        mUrlImage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArtistId);
        dest.writeString(mName);
        dest.writeString(mUrlImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ArtistData> CREATOR = new Creator<ArtistData>() {
        @Override
        public ArtistData createFromParcel(Parcel in) {
            return new ArtistData(in);
        }

        @Override
        public ArtistData[] newArray(int size) {
            return new ArtistData[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getUrlImage() {
        return mUrlImage;
    }

    public void setUrlImage(String urlImage) {
        this.mUrlImage = urlImage;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String artistId) {
        mArtistId = artistId;
    }
}
