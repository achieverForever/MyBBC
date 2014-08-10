package wilson.com.mybbc.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import wilson.com.mybbc.dataset.News;
import wilson.com.mybbc.dataset.TickerNews;

public class MyBBCDB extends SQLiteOpenHelper {
	private static final String DB_NAME = "mybbc.db";
	private static final int DB_VERSION = 2;
	private static final String TABLE_NEWS = "tbl_news";
	private static final String TABLE_TICKER_NEWS = "tbl_ticker_news";
	private static final String CREATE_TABLE_NEWS = "CREATE TABLE IF NOT EXISTS " + TABLE_NEWS
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, summary TEXT, category TEXT,"
			+ " updated TEXT, link TEXT, thumbnail_url TEXT, content TEXT);";
	private static final String CREATE_TABLE_TICKER_NEWS = "CREATE TABLE IF NOT EXISTS " + TABLE_TICKER_NEWS
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, headline TEXT, prompt TEXT, url TEXT,"
			+ " isLive INTEGER, isBreaking INTEGER);";
	private static final String[] COLUMNS_ALL_NEWS = { "id", "title", "summary", "category", "updated",
			"link", "thumbnail_url", "content" };
	private static final String[] COLUMNS_ALL_TICKER_NEWS = { "id", "headline", "prompt", "url", "isLive",
			"isBreaking" };

	public MyBBCDB(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_NEWS);
		db.execSQL(CREATE_TABLE_TICKER_NEWS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TICKER_NEWS);
		onCreate(db);
	}

	public List<News> getNewsOfCategory(String category) {
		List<News> res = null;
		Cursor cursor = getReadableDatabase().query(TABLE_NEWS, COLUMNS_ALL_NEWS, "category=?",
				new String[]{category}, null, null, null);
		while (cursor.moveToNext()) {
			if (res == null) {
				res = new ArrayList<News>();
			}
			News news = new News(cursor.getString(cursor.getColumnIndex("title")),
					cursor.getString(cursor.getColumnIndex("summary")),
					cursor.getString(cursor.getColumnIndex("category")),
					cursor.getString(cursor.getColumnIndex("updated")),
					cursor.getString(cursor.getColumnIndex("link")),
					cursor.getString(cursor.getColumnIndex("thumbnail_url")),
					cursor.getString(cursor.getColumnIndex("content")));
			res.add(news);
		}
		return res;
	}

	public News getNews(String link) {
		News news = null;
		Cursor cursor = getReadableDatabase().query(TABLE_NEWS, COLUMNS_ALL_NEWS, "link=?",
				new String[]{link}, null, null, null);
		if (cursor.moveToNext()) {
			news = new News(cursor.getString(cursor.getColumnIndex("title")),
					cursor.getString(cursor.getColumnIndex("summary")),
					cursor.getString(cursor.getColumnIndex("category")),
					cursor.getString(cursor.getColumnIndex("updated")),
					cursor.getString(cursor.getColumnIndex("link")),
					cursor.getString(cursor.getColumnIndex("thumbnail_url")),
					cursor.getString(cursor.getColumnIndex("content")));
		}
		return news;
	}

	public List<TickerNews> getTickerNews() {
		List<TickerNews> res = null;
		Cursor cursor = getReadableDatabase().query(TABLE_TICKER_NEWS, COLUMNS_ALL_TICKER_NEWS,
				null, null, null, null, null);
		while (cursor.moveToNext()) {
			if (res == null) {
				res = new ArrayList<TickerNews>();
			}
			TickerNews tickerNews = new TickerNews(cursor.getString(cursor.getColumnIndex("id")),
					cursor.getString(cursor.getColumnIndex("headline")),
					cursor.getString(cursor.getColumnIndex("prompt")),
					cursor.getString(cursor.getColumnIndex("url")),
					cursor.getInt(cursor.getColumnIndex("isLive")) == 1 ? true : false,
					cursor.getInt(cursor.getColumnIndex("isBreaking")) == 1 ? true : false);
			res.add(tickerNews);
		}
		return res;
	}

	public void updateNewsOfCategory(String category, List<News> updatedNews) {
		getWritableDatabase().delete(TABLE_NEWS, "category=?", new String[]{category});
		for (News news : updatedNews) {
			ContentValues cv = new ContentValues();
			cv.put("title", news.title);
			cv.put("summary", news.summary);
			cv.put("category", news.category);
			cv.put("updated", news.updated);
			cv.put("link", news.link);
			cv.put("thumbnail_url", news.thumbnailUrl);
			cv.put("content", news.content);

			getWritableDatabase().insert(TABLE_NEWS, null, cv);
		}
	}

	public void updateTickerNews(List<TickerNews> updatedTickerNews) {
		getWritableDatabase().delete(TABLE_TICKER_NEWS, null, null);
		for (TickerNews tn : updatedTickerNews) {
			ContentValues cv = new ContentValues();
			cv.put("headline", tn.headline);
			cv.put("prompt", tn.prompt);
			cv.put("url", tn.url);
			cv.put("isLive", tn.isLive ? 1 : 0);
			cv.put("isBreaking", tn.isBreaking ? 1 : 0);

			getWritableDatabase().insert(TABLE_TICKER_NEWS, null, cv);
		}
	}
}
