package wilson.com.mybbc;


import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import wilson.com.mybbc.dataset.News;
import wilson.com.mybbc.dataset.TickerNews;
import wilson.com.mybbc.networking.RssFeedsParser;
import wilson.com.mybbc.provider.URL;
import wilson.com.mybbc.wiget.FadeInNetworkImageView;

public class HomeFragment extends Fragment {
	private static final String TAG = "HomeFragment";
	private static final int MESSAGE_UPDATE_TICKER_NEWS = 1;

	public static final String EXTRA_ITEM_LIST = "wilson.com.mybbc.items";
	public static final String EXTRA_ITEM_SELECTED = "wilson.com.mybbc.item_selected";
	public static final String EXTRA_CATEGORY = "wilson.com.mybbc.category";

	private TextView mTickerText;

	/**
	 * Current ticker news list, either updated from network or from database
	 */
	private List<TickerNews> mTickerNews;
	/**
	 * Mark if we are newly created or resumed from a paused state
	 */
	private boolean mFirstCreated = true;

	private MyApplication mApp;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_UPDATE_TICKER_NEWS && mTickerNews != null) {
				int curr = msg.arg1;
				int next = (curr + 1) % mTickerNews.size();
				mTickerText.setText(Html.fromHtml("<bold><font color=\"red\">" + mTickerNews.get(next).prompt + ": </font></bold>"
						+ mTickerNews.get(next).headline));
				Message msg2 = Message.obtain();
				msg2.what = MESSAGE_UPDATE_TICKER_NEWS;
				msg2.arg1 = next;
				mHandler.sendMessageDelayed(msg2, 4000);
			}
		}
	};

    public HomeFragment() {
    }

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mApp = (MyApplication) getActivity().getApplication();
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);

		mTickerText = (TextView) rootView.findViewById(R.id.ticker);

		final HorizontalScrollView topStoriesScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.top_stories_scroll);
		final HorizontalScrollView asiaScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.asia_scroll);
		final HorizontalScrollView usCanadaScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.us_canada_scroll);
		final HorizontalScrollView technologyScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.technology_scroll);
		final HorizontalScrollView scienceEnvScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.science_env_scroll);
		final HorizontalScrollView businessScrollView =
				(HorizontalScrollView) rootView.findViewById(R.id.business_scroll);

		final ProgressBar topStoriesProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_top_stories);
		final ProgressBar asiaProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_asia);
		final ProgressBar usCanadaProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_us_canada);
		final ProgressBar techProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_technology);
		final ProgressBar scienceEnvProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_science_environment);
		final ProgressBar businessProgress =
				(ProgressBar) rootView.findViewById(R.id.progress_bar_business);

		final LinearLayout topStoriesContainer =
				(LinearLayout) rootView.findViewById(R.id.top_stories_container);
		final LinearLayout asiaNewsContainer =
				(LinearLayout) rootView.findViewById(R.id.asia_news_container);
		final LinearLayout usCanadaNewsContainer =
				(LinearLayout) rootView.findViewById(R.id.us_canada_news_container);
		final LinearLayout technologyNewsContainer =
				(LinearLayout) rootView.findViewById(R.id.technology_news_container);
		final LinearLayout scienceEnvNewsContainer =
				(LinearLayout) rootView.findViewById(R.id.science_env_news_container);
		final LinearLayout businessNewsContainer =
				(LinearLayout) rootView.findViewById(R.id.business_news_container);

		topStoriesScrollView.setSmoothScrollingEnabled(true);
		topStoriesScrollView.setHorizontalScrollBarEnabled(false);
		asiaScrollView.setSmoothScrollingEnabled(true);
		asiaScrollView.setHorizontalScrollBarEnabled(false);
		usCanadaScrollView.setSmoothScrollingEnabled(true);
		usCanadaScrollView.setHorizontalScrollBarEnabled(false);
		technologyScrollView.setSmoothScrollingEnabled(true);
		technologyScrollView.setHorizontalScrollBarEnabled(false);
		technologyScrollView.setSmoothScrollingEnabled(true);
		scienceEnvScrollView.setHorizontalScrollBarEnabled(false);
		scienceEnvScrollView.setSmoothScrollingEnabled(true);
		businessScrollView.setHorizontalScrollBarEnabled(false);
		businessScrollView.setSmoothScrollingEnabled(true);

		initTickerNews();

		initCategory(URL.TOP_STORIES_URL2, "TOP STORIES", topStoriesContainer, topStoriesProgress, inflater);
		initCategory(URL.ASIA_URL2, "ASIA", asiaNewsContainer, asiaProgress, inflater);
		initCategory(URL.US_CANADA_URL2, "US & CANADA", usCanadaNewsContainer, usCanadaProgress, inflater);
		initCategory(URL.TECHNOLOGY_URL2, "TECHNOLOGY", technologyNewsContainer, techProgress, inflater);
		initCategory(URL.SCIENCE_ENV_URL2, "SCIENCE & ENVIRONMENT", scienceEnvNewsContainer, scienceEnvProgress, inflater);
		initCategory(URL.BUSINESS_URL2, "BUSINESS", businessNewsContainer, businessProgress, inflater);

		mFirstCreated = false;

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		initTickerNews();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(MESSAGE_UPDATE_TICKER_NEWS);
	}

	private void initTickerNews() {
		// Not first created and we already got a list of ticker news, which means we resume from a paused state
		if (!mFirstCreated && mTickerNews != null) {
			Message msg = Message.obtain();
			msg.what = MESSAGE_UPDATE_TICKER_NEWS;
			msg.arg1 = 0;
			mHandler.sendMessage(msg);
		} else {
			// We need to fetch ticker news from network or fail over to use the local ticker news in database
			mApp.addToRequestQueue(new JsonObjectRequest(URL.TICKER_NEWS_URL, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject jsonObject) {
							try {
								mTickerNews = TickerNews.fromJSONArray(jsonObject.getJSONArray("entries"));
								if (mTickerNews != null) {
									Message msg = Message.obtain();
									msg.what = MESSAGE_UPDATE_TICKER_NEWS;
									msg.arg1 = 0;   // Currently displaying ticker news index
									mHandler.sendMessage(msg);
									new StoreTickerNewsToDBThread(mTickerNews).start();
								}
							} catch (JSONException e) {
								Log.e("DEBUG", "Error parsing ticker news - " + e);
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError volleyError) {
							Log.e(TAG, "Fetch ticker news error: " + volleyError);
							Log.d(TAG, "Load ticker news from database instead");
							new LoadTickerNewsFromDBTask().execute();
						}
					}));
		}
	}

	private void initCategory(String url,
	                          final String category,
	                          final ViewGroup container,
	                          final ProgressBar progressBar,
	                          final LayoutInflater inflater) {
		mApp.addToRequestQueue(
				new StringRequest(url,
						new Response.Listener<String>() {
							@Override
							public void onResponse(String s) {
								progressBar.setVisibility(View.INVISIBLE);
								List<News> items = RssFeedsParser.parse(s, category);
								populateScrollView(container, category, inflater, items, true);
								// Store the updated news items to database
								if (items != null) {
									Log.d(TAG, "Updating news items in database");
									new StoreNewsToDBThread(category, items).start();
								}
							}
						},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError volleyError) {
								progressBar.setVisibility(View.INVISIBLE);
								Log.e(TAG, "Fetch feeds error: " + volleyError.getMessage());
								Log.d(TAG, "Load news from local database instead");
								new LoadNewsFromDBTask(container, category, inflater).execute();
							}
						}
				)
		);
	}

	private void populateScrollView(final ViewGroup container,
	                                final String category,
	                                LayoutInflater inflater,
	                                final List<News> items,
	                                boolean networkEnabled) {
		for (int i = 0; i < items.size(); i++) {
			final News item = items.get(i);

			final View newsItem = inflater.inflate(R.layout.item_news_image, container, false);

			TextView titleView = (TextView) newsItem.findViewById(R.id.news_title);
			/*MyNetworkImageView imageView = (MyNetworkImageView) newsItem.findViewById(R.id.image);*/
			FadeInNetworkImageView imageView = (FadeInNetworkImageView) newsItem.findViewById(R.id.image);

			titleView.setText(item.title);
			Bitmap bitmap = null;

			/*// If this bitmap is already in memory cache or disk cache, load it directly, otherwise
			// load it from network if network is enabled.
			if ((bitmap =((HomeActivity) getActivity()).getImageCache().getBitmapFromMemCache(
					item.thumbnailUrl)) != null) {
				Log.d(TAG, "Memory cache hit");
				imageView.setImageBitmap(bitmap);
			} else if ((bitmap = ((HomeActivity) getActivity()).getImageCache().getBitmapFromDiskCache(
					item.thumbnailUrl)) != null){
				Log.d(TAG, "Disk cache hit");
				imageView.setImageBitmap(bitmap);
			} else if (networkEnabled) {
				Log.d(TAG, "Image cache missed, fetch image from network");
				imageView.setImageUrl(item.thumbnailUrl,
									  mApp.getImageLoader(),
									  new MyNetworkImageView.ImageListener() {
									        @Override
											public void onImageLoaded(Bitmap bitmap) {
												if (bitmap != null) {
													// Add bitmap to cache after volley has finished fetching
													// that bitmap from network.
													((HomeActivity) getActivity()).getImageCache()
															.addBitmapToCache(item.thumbnailUrl, bitmap);
												}
											}
									  });

			} else {
				// No cache, no network, done.
			}*/

			imageView.setImageUrl(item.thumbnailUrl, mApp.getImageLoader());

			newsItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
					intent.putParcelableArrayListExtra(EXTRA_ITEM_LIST, (ArrayList)items);
					intent.putExtra(EXTRA_ITEM_SELECTED, container.indexOfChild(newsItem));
					intent.putExtra(EXTRA_CATEGORY, category);
					startActivity(intent);
				}
			});

			container.addView(newsItem);
		}
	}

	private class LoadNewsFromDBTask extends AsyncTask<Void, Void, List<News>> {
		private ViewGroup container;
		private String category;
		private LayoutInflater inflater;

		public LoadNewsFromDBTask(ViewGroup container, String category,
		                          LayoutInflater inflater) {
			this.container = container;
			this.category = category;
			this.inflater = inflater;
		}

		@Override
		protected List<News> doInBackground(Void... params) {
			Log.d(TAG, "Loading news for category: " + category);
			return mApp.getDB().getNewsOfCategory(category);
		}

		@Override
		protected void onPostExecute(List<News> localNews) {
			if (localNews != null) {
				Log.d(TAG, "Loaded " + localNews.size() + " for category: " + category);
				populateScrollView(container, category, inflater, localNews, false);
			}
		}
	}

	private class LoadTickerNewsFromDBTask extends AsyncTask<Void, Void, List<TickerNews>> {
		@Override
		protected List<TickerNews> doInBackground(Void... params) {
			return mApp.getDB().getTickerNews();
		}

		@Override
		protected void onPostExecute(List<TickerNews> localTickerNews) {
			if (localTickerNews != null) {
				mTickerNews = localTickerNews;
				initTickerNews();
			}
		}
	}

	private class StoreNewsToDBThread extends Thread {
		private String category;
		private List<News> items;

		public StoreNewsToDBThread(String category, List<News> items) {
			this.category = category;
			this.items =items;
		}

		@Override
		public void run() {
			mApp.getDB().updateNewsOfCategory(category, items);
		}
	}

	private class StoreTickerNewsToDBThread extends Thread {
		private List<TickerNews> tickerNews;

		public StoreTickerNewsToDBThread(List<TickerNews> tickerNews) {
			this.tickerNews = tickerNews;
		}

		@Override
		public void run() {
			mApp.getDB().updateTickerNews(tickerNews);
		}
	}
}
