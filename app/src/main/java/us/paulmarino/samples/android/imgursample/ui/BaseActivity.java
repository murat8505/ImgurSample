package us.paulmarino.samples.android.imgursample.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import us.paulmarino.samples.android.imgursample.R;

import static us.paulmarino.samples.android.imgursample.util.LogUtils.LOGW;
import static us.paulmarino.samples.android.imgursample.util.LogUtils.makeLogTag;

/**
 * This base activity handles common functionality.
 */
public abstract class BaseActivity extends ActionBarActivity {
    private static final String TAG = makeLogTag(BaseActivity.class);

    // Fade-in and fade-out durations for the main content
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // Toolbar
    private Toolbar mActionbarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionbarToolbar == null) {
            mActionbarToolbar = ButterKnife.findById(
                    BaseActivity.this, R.id.toolbar_actionbar);
            if (mActionbarToolbar != null)
                setSupportActionBar(mActionbarToolbar);
        }
        return mActionbarToolbar;
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
}
