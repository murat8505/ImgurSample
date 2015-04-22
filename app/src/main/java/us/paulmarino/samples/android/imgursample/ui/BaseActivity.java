package us.paulmarino.samples.android.imgursample.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

import butterknife.ButterKnife;
import us.paulmarino.samples.android.imgursample.R;

import static us.paulmarino.samples.android.imgursample.util.LogUtils.LOGW;
import static us.paulmarino.samples.android.imgursample.util.LogUtils.makeLogTag;

/**
 * Base activity for adding some general features and tweaks
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = makeLogTag(BaseActivity.class);

    // Animation Duration
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    // Toolbar
    private Toolbar mActionBarToolbar;

    // Auto-Hide behavior
    private int mActionBarAutoHideSensitivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    private ArrayList<View> mHideableHeaderViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        fadeInMainContent();
    }

    private void fadeInMainContent() {
        View mainContent = ButterKnife.findById(this, R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No main_content view ID to fade in");
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = ButterKnife.findById(
                    this, R.id.toolbar_actionbar);
            if (mActionBarToolbar != null)
                setSupportActionBar(mActionBarToolbar);
        }

        return mActionBarToolbar;
    }

    protected void enableActionBarAutoHide(final RecyclerView recyclerView) {
        initActionBarAutoHide();
        final StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager)
                recyclerView.getLayoutManager();

        if (lm == null)
            return;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            final static int ITEMS_THRESHOLD = 3;
            int lastFirstVisibleItem = 0;
            int[] firstVisibleItems = new int[lm.getSpanCount()];

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                lm.findFirstVisibleItemPositions(firstVisibleItems);
                onMainContentScrolled(
                        firstVisibleItems[0] <= ITEMS_THRESHOLD
                                ? 0
                                : Integer.MAX_VALUE,
                        lastFirstVisibleItem - firstVisibleItems[0] > 0
                                ? Integer.MIN_VALUE
                                : lastFirstVisibleItem == firstVisibleItems[0]
                                ? 0 : Integer.MAX_VALUE
                );
                lastFirstVisibleItem = firstVisibleItems[0];
            }
        });
    }

    private void initActionBarAutoHide() {
        mActionBarAutoHideMinY = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensitivity = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_sensivity);
    }

    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensitivity)
            deltaY = mActionBarAutoHideSensitivity;
        else if (deltaY < -mActionBarAutoHideSensitivity)
            deltaY = -mActionBarAutoHideSensitivity;

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0)
            mActionBarAutoHideSignal = deltaY;
        else
            mActionBarAutoHideSignal += deltaY;

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <= -mActionBarAutoHideSensitivity);
        autoShowOrHideActionBar(shouldShow);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown)
            return;

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        for (View view : mHideableHeaderViews) {
            if (shown) {
                view.animate()
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate()
                        .translationY(-view.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
    }

    /**
     * Called by subclassing Activities. Allows you to enable auto-hide
     * for multiple views.
     */
    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView))
            mHideableHeaderViews.add(hideableHeaderView);
    }
}
