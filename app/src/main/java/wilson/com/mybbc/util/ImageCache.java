package wilson.com.mybbc.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is responsible for manipulating memory cache and disk cache.
 */
public class ImageCache {
	private static final String TAG = "ImageCache";

	// Default memory cache size in kilobytes
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5;  // 5MB

	// Default disk cache size in bytes
	private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1014 * 10;    // 10MB

	// Compression settings when writing images to disk cache
	private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;

	private DiskLruCache mDiskLruCache;
	private LruCache<String, Bitmap> mMemoryCache;

	private final Object mDiskCacheLock = new Object();
	private ImageCacheParams mParams;

	private boolean mDiskCacheStarting = true;

	public static ImageCache getInstance(ImageCacheParams params, FragmentManager fm) {
		// Find or create the RetainFragment
		final RetainFragment retainFragment = findOrCreateRetainFragment(fm);
		// See if we already have an image cache in RetainFragment
		ImageCache imageCache = (ImageCache) retainFragment.getObject();
		// Not existing, create one and store it in retain fragment
		if (imageCache == null) {
			imageCache = new ImageCache(params);
			retainFragment.setObject(imageCache);
		}

		return imageCache;
	}

	private ImageCache(ImageCacheParams params) {
		mParams = params;
		init();
	}

	public void init() {
		Log.d(TAG, "Memory cache created (size= " + mParams.memCacheSize * 1024 + ")");
		mMemoryCache = new LruCache<String, Bitmap>(mParams.memCacheSize) {
			/**
			 * Measure item size in kilobytes.
			 */
			@Override
			protected int sizeOf(String key, Bitmap value) {
				final int bitmapSize = value.getRowBytes() * value.getHeight() / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}
		};
	}

	public void initDiskCache() {
		Log.d(TAG, "Disk cache created (size= " + mParams.diskCacheSize + ")");

		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mParams.diskCacheDir;
				if (!diskCacheDir.exists()) {
					diskCacheDir.mkdirs();
				}
				try {
					mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mParams.diskCacheSize);
				} catch (IOException e) {
					Log.e(TAG, "initDiskCache() - " + e);
				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	public void addBitmapToCache(String url, Bitmap bitmap) {
		if (mMemoryCache != null) {
			mMemoryCache.put(url, bitmap);
		}

		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				final String key = hashKeyForDisk(url);
				OutputStream out = null;
				try {
					DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot == null) {
						DiskLruCache.Editor editor = mDiskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							bitmap.compress(mParams.compressFormat, mParams.compressQuality, out);
							editor.commit();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (IOException e) {
					Log.e(TAG, "addBitmapToCache() - " + e);
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {}
				}
			}
		}
	}

	public Bitmap getBitmapFromMemCache(String url) {
		Bitmap memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(url);
		}

		if (memValue != null) {
			Log.d(TAG, "Memory cache hit!");
		}

		return memValue;
	}

	public Bitmap getBitmapFromDiskCache(String url) {
		final String key = hashKeyForDisk(url);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {}
			}
			if (mDiskLruCache != null) {
				InputStream in = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
					if (snapshot != null) {
						in = snapshot.getInputStream(DISK_CACHE_INDEX);
						bitmap = BitmapFactory.decodeStream(in);
					}
				} catch (IOException e) {
					Log.e(TAG, "getBitmapFromDisk() - " + e);
				} finally {
					try {
						if (in != null) {
							in.close();
						}
					} catch (IOException e) {}
				}
			}
			return bitmap;
		}
	}

	public void clearCache() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			Log.d(TAG, "Memory cache cleared");
		}

		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					Log.d(TAG, "Disk cache cleared");
				} catch (IOException e) {
					Log.e(TAG, "clearCache() - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					Log.d(TAG, "Disk cache flushed");
				} catch (IOException e) {
					Log.e(TAG, "flush() - " + e);
				}
			}
		}
	}

	public void close() {
		synchronized (mDiskCacheLock) {
			try {
				if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
					mDiskLruCache.close();
					mDiskLruCache = null;
				}
			} catch (IOException e) {
				Log.e(TAG, "close() - " + e);
			}
		}
	}

	private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
		RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
		if (fragment == null) {
			fragment = new RetainFragment();
			fm.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
		}

		return fragment;
	}

	public static String hashKeyForDisk(String key) {
		String cacheKey;
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes());
			cacheKey = bytesToHexString(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath =
				Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
						context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;
		public Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;

		public ImageCacheParams(Context context, String diskCacheDir) {
			this.diskCacheDir = getDiskCacheDir(context, diskCacheDir);
		}
	}

	public static class RetainFragment extends Fragment {
		private Object mObject;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		public void setObject(Object obj) {
			mObject = obj;
		}

		public Object getObject() {
			return mObject;
		}
	}
}
