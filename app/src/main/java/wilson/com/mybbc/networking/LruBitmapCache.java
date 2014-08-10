package wilson.com.mybbc.networking;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache{
	private static final String TAG = "LruBitmapCache";
	
	public static int getDefaultLruCacheSize() {
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		return maxMemory / 8;
	}

	public LruBitmapCache(int cacheSizeInKB) {
		super(cacheSizeInKB);
	}

	public LruBitmapCache() {
		this(getDefaultLruCacheSize());
	}

	@Override
	public Bitmap getBitmap(String url) {
		return get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
	}

	@Override
	protected int sizeOf(String key, Bitmap value) {
		return value.getRowBytes() * value.getHeight() / 1024;
	}

}
