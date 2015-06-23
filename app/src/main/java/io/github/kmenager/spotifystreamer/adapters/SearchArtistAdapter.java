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
import io.github.kmenager.spotifystreamer.model.ArtistData;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;


public class SearchArtistAdapter extends RecyclerView.Adapter<SearchArtistAdapter.SearchArtistAdapterHolder> {

    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private List<Artist> mArtists;
    final private Context mContext;
    private final View mHeader;
    final private SearchArtistAdapterOnClickHandler mClickHandler;

    public SearchArtistAdapter(Context context, List<Artist> artists, View header, SearchArtistAdapterOnClickHandler clickHandler) {
        mContext = context;
        mArtists = artists;
        mHeader = header;
        mClickHandler = clickHandler;
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    public interface SearchArtistAdapterOnClickHandler {
        void onClick(ArtistData artistData, SearchArtistAdapterHolder vh);
    }

    @Override
    public SearchArtistAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            return new SearchArtistAdapterHolder(mHeader, ITEM_VIEW_TYPE_HEADER);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_artist, parent, false);
        return new SearchArtistAdapterHolder(view, ITEM_VIEW_TYPE_ITEM);
    }

    @Override
    public void onBindViewHolder(SearchArtistAdapterHolder holder, int position) {
        if (isHeader(position)) {
            return;
        }
        holder.bindView(mArtists.get(position - 1));
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (mArtists == null)
            return 0;
        else
            return mArtists.size() + 1;
    }

    public void reset(List<Artist> artists) {
        if (mArtists != null) {
            mArtists.clear();
            if (artists != null) {
                mArtists.addAll(artists);
            }

        } else {
            mArtists = artists;
        }
        notifyDataSetChanged();
    }

    public class SearchArtistAdapterHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView mImageViewIcon;
        public TextView mTextViewName;
        private Artist mArtist;
        public SearchArtistAdapterHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == ITEM_VIEW_TYPE_ITEM) {
                mImageViewIcon = (ImageView) itemView.findViewById(R.id.artist_icon);
                mTextViewName = (TextView) itemView.findViewById(R.id.artist_name);
                itemView.setOnClickListener(this);
            }
        }

        public void bindView(Artist artist) {
            mArtist = artist;
            List<Image> images = artist.images;
            mImageViewIcon.setImageResource(R.drawable.ic_empty_artist);
            if (!images.isEmpty()) {
                Glide.with(mContext)
                        .load(images.get(0).url)
                        .placeholder(R.drawable.ic_empty_artist)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mImageViewIcon);
            }
            mTextViewName.setText(artist.name);
        }

        @Override
        public void onClick(View v) {
            ArtistData artistData = new ArtistData();
            artistData.setArtistId(mArtist.id);
            artistData.setName(mArtist.name);
            if (!mArtist.images.isEmpty()) {
               artistData.setUrlImage(mArtist.images.get(0).url);
            }
            mClickHandler.onClick(artistData, this);
        }
    }
}
