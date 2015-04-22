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
import us.paulmarino.samples.android.imgursample.util.UIUtils;

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

    private int mRecyclerViewTopClearance = 0;

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
        updateContentTopClearance();

        // Configure views
        setupSwipeRefresh();
        setupRecyclerView();

        // Load gallery data and set progress
        mApiClient.loadGallery(SUBREDDIT_CATEGORY_SPACE, this);
        onRefreshingStateChanged(true);

        return v;
    }

    /**
     * Sets proper margins to take into account our Toolbar
     * or other header views.
     */
    private void updateContentTopClearance() {
        int actionBarClearance = UIUtils.calculateActionBarSize(getActivity());
        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.grid_item_spacing);

        setRecyclerViewTopClearance(actionBarClearance);
        setSwipeRefreshTopClearance(actionBarClearance + gridSpacing);
    }

    private void setRecyclerViewTopClearance(int topClearance) {
        if (mRecyclerView == null)
            return;

        if (mRecyclerViewTopClearance != topClearance) {
            mRecyclerViewTopClearance = topClearance;

            int paddingLeft = mRecyclerView.getPaddingLeft();
            int paddingRight = mRecyclerView.getPaddingRight();
            int paddingBottom = mRecyclerView.getPaddingBottom();

            mRecyclerView.setPadding(paddingLeft, mRecyclerViewTopClearance,
                    paddingRight, paddingBottom);
        }
    }

    private void setSwipeRefreshTopClearance(int topClearance) {
        if (mSwipeRefreshLayout == null)
            return;

        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        mSwipeRefreshLayout.setProgressViewOffset(false,
                0, topClearance + progressBarEndMargin);
    }

    /**
     * Configure SwipeRefresh colors and listeners
     */
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

        // Layout Manager
        int gridSpan = res.getInteger(R.integer.gallery_grid_columns);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return mRecyclerView != null && ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    @Override
    public void onGalleryLoaded(ArrayList<GalleryItem> data) {
        mGalleryAdapter.setGalleryItems(data);
        mGalleryAdapter.notifyDataSetChanged();

        // stop the refreshing
        onRefreshingStateChanged(false);

        LOGD(TAG, "onGalleryLoaded: items=" + (data != null ? data.size() : "null"));
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
            mGalleryItems = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext)
                    .inflate(R.layout.list_item_gallery, parent, false);

            return new GalleryViewHolder(itemView);
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
             *
             * use GalleryThumbnail.SIZE_LARGE for higher resolution thumbnails.
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
