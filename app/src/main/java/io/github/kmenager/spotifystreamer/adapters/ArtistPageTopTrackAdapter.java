package io.github.kmenager.spotifystreamer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.model.TrackData;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


public class ArtistPageTopTrackAdapter extends RecyclerView.Adapter<ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterHolder> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private final View mHeader;
    private final Context mContext;
    private final ArtistPageTopTrackAdapterOnClickHandler mClickHandler;
    private List<Track> mTracks;

    public ArtistPageTopTrackAdapter(Context context, List<Track> tracks, View header, ArtistPageTopTrackAdapterOnClickHandler clickHandler) {
        mContext = context;
        mTracks = tracks;
        mHeader = header;
        mClickHandler = clickHandler;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    public interface ArtistPageTopTrackAdapterOnClickHandler {
        void onClick(TrackData trackData, ArtistPageTopTrackAdapterHolder vh);
    }

    @Override
    public ArtistPageTopTrackAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            return new ArtistPageTopTrackAdapterHolder(mHeader, ITEM_VIEW_TYPE_HEADER);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_track, parent, false);
        return new ArtistPageTopTrackAdapterHolder(view, ITEM_VIEW_TYPE_ITEM);
    }

    @Override
    public void onBindViewHolder(ArtistPageTopTrackAdapterHolder holder, int position) {
        if (isHeader(position)) {
            return;
        }
        holder.bindView(mTracks.get(position - 1));
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (mTracks != null) return mTracks.size() + 1;
        return 0;
    }

    public void reset(List<Track> tracks) {
        mTracks.clear();
        if (tracks != null) {
            mTracks.addAll(tracks);
        }
        notifyDataSetChanged();
    }

    public class ArtistPageTopTrackAdapterHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @InjectView(R.id.track_icon)
        ImageView mTrackIcon;
        @InjectView(R.id.song_name)
        TextView mSongName;
        @InjectView(R.id.artist_name)
        TextView mArtistName;

        Track mTrack;

        public ArtistPageTopTrackAdapterHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == ITEM_VIEW_TYPE_ITEM) {
                ButterKnife.inject(this, itemView);
                itemView.setOnClickListener(this);
            }
        }

        public void bindView(Track track) {
            mTrack = track;

            mSongName.setText(mTrack.name);
            List<Image> images = mTrack.album.images;
            if (!images.isEmpty()) {
                Glide.with(mContext)
                        .load(images.get(0).url)
                        .placeholder(R.drawable.ic_album_empty)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mTrackIcon);
            }
            else {
                mTrackIcon.setImageResource(R.drawable.ic_album_empty);
            }
            List<ArtistSimple> artistSimples = mTrack.artists;
            if (!artistSimples.isEmpty()) {
                mArtistName.setText(artistSimples.get(0).name);
            }

        }

        @Override
        public void onClick(View v) {
            TrackData trackData = new TrackData();
            trackData.setName(mTrack.name);
            List<ArtistSimple> artistSimples = mTrack.artists;
            if (!artistSimples.isEmpty()) {
                trackData.setArtistName(artistSimples.get(0).name);
            }

            trackData.setDuration(mTrack.duration_ms);
            trackData.setPreviewUrl(mTrack.preview_url);
            List<Image> images = mTrack.album.images;
            if (!images.isEmpty()) {
                trackData.setUrlAlbum(images.get(0).url);
            }
            mClickHandler.onClick(trackData, this);
        }
    }
}
