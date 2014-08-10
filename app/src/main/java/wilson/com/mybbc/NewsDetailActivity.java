package wilson.com.mybbc;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.LinePageIndicator;

import java.util.List;

import wilson.com.mybbc.dataset.News;

public class NewsDetailActivity extends FragmentActivity {

	private ViewPager mViewPager;
	private MyAdapter mAdapter;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

		List<News> items = null;
		int selectedItem = -1;
		String category = null;
		if (getIntent() == null
				|| (category = getIntent().getStringExtra(HomeFragment.EXTRA_CATEGORY)) == null
				|| (selectedItem = getIntent().getIntExtra(HomeFragment.EXTRA_ITEM_SELECTED, -1)) < 0
				|| (items = getIntent().getParcelableArrayListExtra(HomeFragment.EXTRA_ITEM_LIST)) == null) {
			return;
		}

	    ActionBar actionBar = getActionBar();
	    if (actionBar != null) {
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setDisplayShowTitleEnabled(true);
	    }

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		TextView categoryText = (TextView) findViewById(R.id.category);
		LinePageIndicator pagerIndicator = (LinePageIndicator) findViewById(R.id.pager_indicator);

		categoryText.setText(category);

		mAdapter = new MyAdapter(getSupportFragmentManager(), items);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(selectedItem);

		pagerIndicator.setViewPager(mViewPager, selectedItem);
		final float density = getResources().getDisplayMetrics().density;
		pagerIndicator.setSelectedColor(0x88FF0000);
		pagerIndicator.setUnselectedColor(0xFF888888);
		pagerIndicator.setStrokeWidth(4 * density);
		pagerIndicator.setLineWidth(30 * density);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case android.R.id.home:
			    onBackPressed();
			    return true;
		    case R.id.action_settings:
			    Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
			    return true;
		    case R.id.action_refresh:
			    Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
			    return true;
		    case R.id.action_share:
			    Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
			    return true;
		    default:
			    return super.onOptionsItemSelected(item);
	    }
    }

	public static class MyAdapter extends FragmentStatePagerAdapter {
		private List<News> mItems;

		public MyAdapter(FragmentManager fm, List<News> items) {
			super(fm);
			mItems = items;
		}

		@Override
		public Fragment getItem(int position) {
			return NewsDetailFragment.newInstance(mItems.get(position));
		}

		@Override
		public int getCount() {
			return mItems.size();
		}
	}
}
