package wilson.com.mybbc;

import android.app.Activity;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import wilson.com.mybbc.networking.RssFeedsParser;
import wilson.com.mybbc.util.ImageCache;


public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	private static final String DISK_CACHE_DIR = "cache";

	private static final int MESSAGE_INIT_DISK_CACHE = 0;
	private static final int MESSAGE_FLUSH_DISK_CACHE = 1;
	private static final int MESSAGE_CLEAR_CACHE = 2;
	private static final int MESSAGE_CLOSE_DISK_CACHE = 3;

	private ImageCache mCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_home);
	    if (savedInstanceState == null) {
		    getFragmentManager().beginTransaction()
				    .add(R.id.container, new HomeFragment())
				    .commit();
	    }
	    mCache = ImageCache.getInstance(new ImageCache.ImageCacheParams(this, DISK_CACHE_DIR),
			    getFragmentManager());

	    new ImageCacheTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
	    switch (id) {
		    case R.id.action_settings:
			    return true;
		    case R.id.action_refresh:
			    Toast.makeText(this, "Refresh", Toast.LENGTH_LONG).show();
			    return true;
		    case R.id.action_edit:
			    Toast.makeText(this, "Edit", Toast.LENGTH_LONG).show();
			    return true;
		    default:
			    return super.onOptionsItemSelected(item);
	    }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((MyApplication) getApplication()).cancelPendingRequests(MyApplication.TAG);
	}

	private class ImageCacheTask extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			switch (params[0]) {
				case MESSAGE_INIT_DISK_CACHE:
					mCache.initDiskCache();
					break;
				case MESSAGE_CLEAR_CACHE:
					mCache.clearCache();
					break;
				case MESSAGE_FLUSH_DISK_CACHE:
					mCache.flush();
					break;
				case MESSAGE_CLOSE_DISK_CACHE:
					mCache.close();
					break;
			}
			return null;
		}
	}

	public ImageCache getImageCache() {
		return mCache;
	}

}
