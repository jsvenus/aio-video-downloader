package views.Sliding;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import com.softcsoftware.aio.R;

public class SlidingView extends ViewGroup {

    public final static int DURATION = 400; // time to show sliding animation

    protected boolean place_left = true;
    protected boolean is_open;
    protected View slide_bar;
    protected View content_bar;

    protected int slide_bar_width = -1;

    protected Animation animation;
    protected OpenListener open_listener;
    protected CloseListener close_listener;
    protected Listener listener;

    protected boolean is_press = false;

    public SlidingView(Context context) {
        this(context, null);
    }

    public SlidingView
            (Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public View get_slide_view() {
        return this.slide_bar;
    }

    public View get_content_view() {
        return this.slide_bar;
    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        slide_bar = findViewById(R.id.slide_bar_list_activity);
        content_bar = findViewById(R.id.content_bar_list_activity);

        if (slide_bar == null) {
            throw new NullPointerException("no view id = animation_sidebar");
        }

        if (content_bar == null) {
            throw new NullPointerException("no view id = animation_content");
        }

        open_listener = new OpenListener(slide_bar, content_bar);
        close_listener = new CloseListener(slide_bar, content_bar);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
                /* the title bar assign top padding, drop it */
        int sidebarLeft = l;
        if (!place_left) {
            sidebarLeft = r - slide_bar_width;
        }
        slide_bar.layout(sidebarLeft,
                0,
                sidebarLeft + slide_bar_width,
                0 + slide_bar.getMeasuredHeight());

        if (is_open) {
            if (place_left) {
                content_bar.layout(l + slide_bar_width, 0, r + slide_bar_width, b);
            } else {
                content_bar.layout(l - slide_bar_width, 0, r - slide_bar_width, b);
            }
        } else {
            content_bar.layout(l, 0, r, b);
        }
    }

    @Override
    public void onMeasure(int w, int h) {
        super.onMeasure(w, h);
        super.measureChildren(w, h);
        slide_bar_width = slide_bar.getMeasuredWidth();
    }

    @Override
    protected void measureChild(View child, int parentWSpec, int parentHSpec) {
        if (child == slide_bar) {
            int mode = MeasureSpec.getMode(parentWSpec);
            int width = (int) (getMeasuredWidth() * 0.85);
            super.measureChild(child, MeasureSpec.makeMeasureSpec(width, mode), parentHSpec);
        } else {
            super.measureChild(child, parentWSpec, parentHSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isOpening()) {
            return false;
        }

        int action = ev.getAction();

        if (action != MotionEvent.ACTION_UP
                && action != MotionEvent.ACTION_DOWN) {
            return false;
        }

				/* if user press and release both on Content while
                 * sidebar is opening, call listener. otherwise, pass
				 * the event to child. */
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (content_bar.getLeft() < x
                && content_bar.getRight() > x
                && content_bar.getTop() < y
                && content_bar.getBottom() > y) {
            if (action == MotionEvent.ACTION_DOWN) {
                is_press = false;
            }

            if (is_press
                    && action == MotionEvent.ACTION_UP
                    && listener != null) {
                is_press = false;
                return listener.onContentTouchedWhenOpening();
            }
        } else {
            is_press = false;
        }

        return false;
    }

    public void setListener(Listener _listener) {
        listener = _listener;
    }

    public boolean isOpening() {
        return is_open;
    }

    public void toggleSidebar() {
        if (content_bar.getAnimation() != null) {
            return;
        }

        if (is_open) {
                        /* opened, make close animation*/
            if (place_left) {
                animation = new TranslateAnimation(0, -slide_bar_width, 0, 0);
            } else {
                animation = new TranslateAnimation(0, slide_bar_width, 0, 0);
            }
            animation.setAnimationListener(close_listener);
        } else {
                        /* not opened, make open animation */
            if (place_left) {
                animation = new TranslateAnimation(0, slide_bar_width, 0, 0);
            } else {
                animation = new TranslateAnimation(0, -slide_bar_width, 0, 0);
            }
            animation.setAnimationListener(open_listener);
        }
        animation.setDuration(DURATION);
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        content_bar.startAnimation(animation);
    }

    public void openSidebar() {
        if (!is_open) {
            toggleSidebar();
        }
    }

    public void closeSidebar() {
        if (is_open) {
            toggleSidebar();
        }
    }

    public interface Listener {
        public void onSidebarOpened();

        public void onSidebarClosed();

        public boolean onContentTouchedWhenOpening();
    }

    class OpenListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        OpenListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            iSidebar.setVisibility(View.VISIBLE);
            findViewById(R.id.download_manager).setClickable(true);
        }

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            is_open = !is_open;
            requestLayout();
            if (listener != null) {
                listener.onSidebarOpened();
            }
        }
    }

    class CloseListener implements Animation.AnimationListener {
        View iSidebar;
        View iContent;

        CloseListener(View sidebar, View content) {
            iSidebar = sidebar;
            iContent = content;
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            iContent.clearAnimation();
            iSidebar.setVisibility(View.GONE);
            findViewById(R.id.download_manager).setClickable(false);
            is_open = !is_open;
            requestLayout();
            if (listener != null) {
                listener.onSidebarClosed();
            }
        }
    }
}

