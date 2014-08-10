package wilson.com.mybbc.wiget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
	private static final String TAG = "MyScrollView";

	private float mDeltaX;
	private float mDeltaY;
	private float mLastX;
	private float mLastY;

	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
/*		Log.d(TAG, String.format("MotionEvent: %s, x: %f, y: %f",
				actionToString(ev.getAction()), ev.getX(), ev.getY()));*/
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDeltaX = mDeltaY = 0.0f;
				mLastX = ev.getX();
				mLastY = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				mDeltaX += Math.abs(ev.getX() - mLastX);
				mDeltaY += Math.abs(ev.getY() - mLastY);
				mLastX = ev.getX();
				mLastY = ev.getY();
				if (mDeltaX > mDeltaY) {
					return false;
				}
		}

		return super.onInterceptTouchEvent(ev);
	}

	public static String actionToString(int action) {
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				return "ACTION_DOWN";
			case MotionEvent.ACTION_UP:
				return "ACTION_UP";
			case MotionEvent.ACTION_CANCEL:
				return "ACTION_CANCEL";
			case MotionEvent.ACTION_OUTSIDE:
				return "ACTION_OUTSIDE";
			case MotionEvent.ACTION_MOVE:
				return "ACTION_MOVE";
			case MotionEvent.ACTION_HOVER_MOVE:
				return "ACTION_HOVER_MOVE";
			case MotionEvent.ACTION_SCROLL:
				return "ACTION_SCROLL";
			case MotionEvent.ACTION_HOVER_ENTER:
				return "ACTION_HOVER_ENTER";
			case MotionEvent.ACTION_HOVER_EXIT:
				return "ACTION_HOVER_EXIT";
			default:
				return "UNKNOWN_ACTION";
		}
	}
}
















