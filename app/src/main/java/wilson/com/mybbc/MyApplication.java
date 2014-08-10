package wilson.com.mybbc;

import android.app.Application;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import wilson.com.mybbc.networking.LruBitmapCache;
import wilson.com.mybbc.util.ImageCache;
import wilson.com.mybbc.util.MyBBCDB;

public class MyApplication extends Application {
	public static final String TAG = "MyApplication";

	private static MyApplication sInstance;

	private RequestQueue mRequestQueue;

	private ImageLoader mImageLoader;

	private MyBBCDB mDB;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		mDB = new MyBBCDB(this);
	}

	public static synchronized MyApplication getInstance() {
		return sInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(getRequestQueue(), new LruBitmapCache());
		}
		return mImageLoader;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(String tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

	public MyBBCDB getDB() {
		return mDB;
	}
}
