package wilson.com.mybbc.dataset;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TickerNews {
	private static final String TAG = "TickerNews";

	public String id;
	public String headline;

	public TickerNews() {
	}

	public TickerNews(String id, String headline, String prompt, String url, boolean isLive,
	                  boolean isBreaking) {
		this.id = id;
		this.headline = headline;
		this.prompt = prompt;
		this.url = url;
		this.isLive = isLive;
		this.isBreaking = isBreaking;
	}

	public String prompt;
	public String url;

	public boolean isLive;
	public boolean isBreaking;


	public static TickerNews fromJSON(JSONObject json) {
		if (json == null) {
			return null;
		}
		TickerNews news = new TickerNews();
		try {
			news.id = json.getString("id");
			news.headline = json.getString("headline");
			news.prompt = json.getString("prompt");
			news.url = json.getString("url");
			news.isLive = json.getString("isLive").equals("false") ? false : true;
			news.isBreaking = json.getString("isBreaking").equals("false") ? false : true;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing ticker news - " + e);
		}
		return news;
	}

	public static List<TickerNews> fromJSONArray(JSONArray jsonArray) {
		if (jsonArray == null) {
			return null;
		}
		List<TickerNews> newsList = new ArrayList<TickerNews>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				newsList.add(fromJSON(jsonArray.getJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing ticker news - " + e);
		}
		return newsList;
	}


	@Override
	public String toString() {
		return "TickerNews{" +
				"id='" + id + '\'' +
				", headline='" + headline + '\'' +
				", prompt='" + prompt + '\'' +
				", url='" + url + '\'' +
				", isLive=" + isLive +
				", isBreaking=" + isBreaking +
				'}';
	}
}
