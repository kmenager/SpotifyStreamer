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

import io.github.kmenager.spotifystreamer.R;
import io.github.kmenager.spotifystreamer.model.TrackData;


public class ArtistPageTopTrackAdapter extends RecyclerView.Adapter<ArtistPageTopTrackAdapter.ArtistPageTopTrackAdapterHolder> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private final View mHeader;
    private final Context mContext;
    private final ArtistPageTopTrackAdapterOnClickHandler mClickHandler;
    private List<TrackData> mTracks;

    public ArtistPageTopTrackAdapter(Context context, List<TrackData> tracks, View header, ArtistPageTopTrackAdapterOnClickHandler clickHandler) {
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
        if (mTracks != null)
            return mTracks.size() + 1; // The first element is the list title, so we add + 1
        return 0;
    }

    public void reset(List<TrackData> tracks) {
        mTracks.clear();
        if (tracks != null) {
            mTracks.addAll(tracks);
        }
        notifyDataSetChanged();
    }

    public class ArtistPageTopTrackAdapterHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImageView mTrackIcon;
        private TextView mSongName;
        private TextView mArtistName;

        TrackData mTrack;

        public ArtistPageTopTrackAdapterHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == ITEM_VIEW_TYPE_ITEM) {
                mTrackIcon = (ImageView) itemView.findViewById(R.id.track_icon);
                mSongName = (TextView) itemView.findViewById(R.id.song_name);
                mArtistName = (TextView) itemView.findViewById(R.id.artist_name);
                itemView.setOnClickListener(this);
            }
        }

        public void bindView(TrackData track) {
            mTrack = track;

            mSongName.setText(mTrack.getName());
            if (mTrack.getUrlAlbum() != null) {
                Glide.with(mContext)
                        .load(mTrack.getUrlAlbum())
                        .placeholder(R.drawable.ic_album_empty)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(mTrackIcon);
            } else {
                mTrackIcon.setImageResource(R.drawable.ic_album_empty);
            }

            mArtistName.setText(mTrack.getArtistName());


        }

        @Override
        public void onClick(View v) {
            mClickHandler.onClick(mTrack, this);
        }
    }
}
