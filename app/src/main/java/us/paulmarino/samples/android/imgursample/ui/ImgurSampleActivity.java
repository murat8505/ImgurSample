package us.paulmarino.samples.android.imgursample.ui;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import us.paulmarino.samples.android.imgursample.R;
import us.paulmarino.samples.android.imgursample.ui.widget.DrawShadowFrameLayout;
import us.paulmarino.samples.android.imgursample.util.UIUtils;

public class ImgurSampleActivity extends BaseActivity {

    @InjectView(R.id.main_content) DrawShadowFrameLayout mDrawShadowFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgur);

        ButterKnife.inject(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        RecyclerView recyclerView = ButterKnife.findById(this, R.id.recycler_view);
        if (recyclerView != null)
            enableActionBarAutoHide(recyclerView);

        registerHideableHeaderView(ButterKnife.findById(this, R.id.toolbar_actionbar));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateToolbarShadowTopOffset();
    }

    /**
     * Sets the header shadow drawable top offset (pre-L)
     */
    private void updateToolbarShadowTopOffset() {
        int actionBarClearance = UIUtils.calculateActionBarSize(this);
        mDrawShadowFrameLayout.setShadowTopOffset(actionBarClearance);
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        mDrawShadowFrameLayout.setShadowVisible(shown, shown);
    }
}
