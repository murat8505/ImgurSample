package us.paulmarino.samples.android.imgursample.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.paulmarino.samples.android.imgursample.R;
import us.paulmarino.samples.android.imgursample.api.ImgurApiClient;
import us.paulmarino.samples.android.imgursample.model.GalleryItem;
import us.paulmarino.samples.android.imgursample.ui.widget.MultiSwipeRefreshLayout;
import us.paulmarino.samples.android.imgursample.util.GalleryThumbnail;

import static us.paulmarino.samples.android.imgursample.util.LogUtils.LOGD;
import static us.paulmarino.samples.android.imgursample.util.LogUtils.makeLogTag;

/**
 * A fragment that shows items from the ImgurApiClient
 */
public class ImgurSampleFragment extends Fragment implements
        MultiSwipeRefreshLayout.CanChildScrollUpCallback, ImgurApiClient.GalleryLoadedListener {
    private static final String TAG = makeLogTag(ImgurSampleFragment.class);

    private static final String SUBREDDIT_CATEGORY_SPACE = "space";

    @InjectView(R.id.swipe_refresh) MultiSwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.recycler_view) RecyclerView mRecyclerView;

    private GalleryAdapter mGalleryAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ImgurApiClient mApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch our API client instance
        mApiClient = ImgurApiClient.get(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_imgur, container, false);
        ButterKnife.inject(this, v);

        // Configure Views
        setupSwipeRefresh();
        setupRecyclerView();

        // Load Gallery Data
        mApiClient.loadGallery(SUBREDDIT_CATEGORY_SPACE, this);
        setInitialRefreshProgress();

        return v;
    }

    private void setupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mApiClient.loadGallery(SUBREDDIT_CATEGORY_SPACE, ImgurSampleFragment.this);
                }
            });

            mSwipeRefreshLayout.setCanChildScrollUpCallback(this);
        }
    }

    /**
     * Configure RecyclerView and its components
     */
    private void setupRecyclerView() {
        final Resources res = getResources();
        int gridSpan = res.getInteger(R.integer.gallery_grid_columns);

        // Layout Manager
        mLayoutManager = new StaggeredGridLayoutManager(gridSpan,
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mGalleryAdapter = new GalleryAdapter(getActivity());
        mRecyclerView.setAdapter(mGalleryAdapter);

        // Decoration (Margins)
        int itemSpacing = res.getDimensionPixelSize(R.dimen.grid_item_spacing);
        mRecyclerView.addItemDecoration(new GalleryItemDecoration(itemSpacing));
    }

    private void setInitialRefreshProgress() {
        // Allows us to start refreshing without waiting for onMeasure
        mSwipeRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                        getResources().getDisplayMetrics()));

        onRefreshingStateChanged(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        if (mRecyclerView != null)
            return ViewCompat.canScrollVertically(mRecyclerView, -1);

        return false;
    }

    @Override
    public void onGalleryLoaded(ArrayList<GalleryItem> data) {
        mGalleryAdapter.setGalleryItems(data);
        mGalleryAdapter.notifyDataSetChanged();

        // stop the refreshing
        onRefreshingStateChanged(false);
        updateSwipeRefreshProgressBarTop();

        LOGD(TAG, "onGalleryLoaded: items=" + data.size());
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null)
            return;

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        mSwipeRefreshLayout.setProgressViewOffset(false,
                progressBarStartMargin, progressBarEndMargin);
    }

    private void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    /**
     * Adapter for our Gallery items
     */
    private static class GalleryAdapter extends RecyclerView.Adapter {
        private Context mContext;
        private ArrayList<GalleryItem> mGalleryItems;

        public GalleryAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_gallery, parent, false);

            GalleryViewHolder holder = new GalleryViewHolder(itemView);

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            if (item == null)
                return;

            ((GalleryViewHolder) holder).galleryTitleText.setText(item.title);

            /*
             * Generate a thumbnail url here, instead of trying to load the
             * potentially large images as returned by "link" in the data response
             */
            final String thumbnailUrl = GalleryThumbnail.buildThumbnailRequestUrl(
                    item.id, GalleryThumbnail.SIZE_MEDIUM);

            // load the thumbnail "cover" image
            Picasso.with(mContext)
                    .load(thumbnailUrl)
                    .into(((GalleryViewHolder) holder).galleryImageView);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems != null ? mGalleryItems.size() : 0;
        }

        public void setGalleryItems(ArrayList<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }
    }

    /**
     * Simple ViewHolder for our gallery items
     */
    static class GalleryViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.gallery_item_image) ImageView galleryImageView;
        @InjectView(R.id.gallery_item_title) TextView galleryTitleText;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    /**
     * Used as a custom spacing rule for the grid items
     */
    private static class GalleryItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpace;

        public GalleryItemDecoration(int space) {
            mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace / 2;
            outRect.top = mSpace;
        }
    }
}
