package wilson.com.mybbc.networking;

import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wilson.com.mybbc.dataset.News;


public class RssFeedsParser {

	private static final String TAG = "FeedsXMLParser";
	private static final String IMAGE_BASE_URL = "http://news.bbcimg.co.uk/media/images/";


/*	*//*
	 * Parse the XML and return a list of Items.
	 *//*
	public static List<Item> parse(String xml)
			throws IOException, XmlPullParserException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();

		parser.setInput(new StringReader(xml));
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.nextTag();   // <rss>
		Log.d(TAG, parser.getName());
		parser.nextTag();   // <channel>
		Log.d(TAG, parser.getName());

		return readChannel(parser);
	}

	private static List<Item> readChannel(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, "channel");
		List<Item> entries = new ArrayList<Item>();

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;

			if (parser.getName().equals("item")) {
				entries.add(readItem(parser));
			} else {
				skipOneTag(parser);
			}
		}

		return entries;
	}

	private static Item readItem(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, "item");
		Item item = new Item();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;

			String tag = parser.getName();
			if (tag.equals("title")) {
				item.title = readText(parser);
			} else if (tag.equals("description")) {
				item.desc = readText(parser);
			} else if (tag.equals("link")) {
				item.link = readText(parser).replace("www.bbc.co.uk", "www.bbc.com");
			} else if (tag.equals("guid")) {
				item.guid = readText(parser);
			} else if (tag.equals("pubDate")) {
				item.pubDate = readText(parser);
			} else if (tag.equals("media:thumbnail") && parser.getAttributeValue(0).equals("144")) {
				item.thumbnailUrl = parser.getAttributeValue(2);
				parser.nextTag();
			} else {
				skipOneTag(parser);
			}
		}
		return item;
	}

	private static void skipOneTag(XmlPullParser parser)
			throws IOException, XmlPullParserException {

		if (parser.getEventType() != XmlPullParser.START_TAG)
			throw new IllegalStateException();

		int depth = 1;		// To keep track of the current depth we are in
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

	private static String readText(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		String text = "";
		if (parser.next() == XmlPullParser.TEXT) {
			text = parser.getText();
			parser.nextTag();	// Consume the TAG_END event
		}
		return text;
	}

	public static class Item implements Parcelable {
		public String title;
		public String desc;
		public String link;
		public String guid;
		public String pubDate;
		public String thumbnailUrl;

		public Item() {
		}

		public Item(String title, String desc,String link, String guid, String pubDate,
		            String thumbnailUrl) {
			this.title = title;
			this.desc = desc;
			this.link = link;
			this.guid = guid;
			this.pubDate = pubDate;
			this.thumbnailUrl = thumbnailUrl;
		}

		public static final Creator CREATOR = new Creator<Item>() {
			@Override
			public Item createFromParcel(Parcel source) {
				Item item = new Item();
				item.title = source.readString();
				item.desc = source.readString();
				item.link = source.readString();
				item.guid = source.readString();
				item.pubDate = source.readString();
				item.thumbnailUrl = source.readString();
				return item;
			}

			@Override
			public Item[] newArray(int size) {
				return new Item[size];
			}
		};

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(title);
			dest.writeString(desc);
			dest.writeString(link);
			dest.writeString(guid);
			dest.writeString(pubDate);
			dest.writeString(thumbnailUrl);
		}

		@Override
		public int describeContents() {
			return super.hashCode();
		}

		@Override
		public String toString() {
			return "Item{" +
					"title='" + title + '\'' +
					", desc='" + desc + '\'' +
					", link='" + link + '\'' +
					", guid='" + guid + '\'' +
					", pubDate='" + pubDate + '\'' +
					", thumbnailUrl='" + thumbnailUrl + '\'' +
					'}';
		}
	}*/


	public static List<News> parse(String feed, String category) {
		List<News> res = null;
		if (TextUtils.isEmpty(feed)) {
			return null;
		}

		Document doc = Parser.parse(feed, "");
		Elements entries = doc.select("entry");
		for (Element e : entries) {
			if (res == null) {
				res = new ArrayList<News>();
			}
			News news = new News();
			news.title = e.select("title").text();
			news.summary = e.select("summary").text();
			news.category = category;
			news.updated = e.select("updated").text();
			news.link = e.select("link").attr("href");
			news.thumbnailUrl = transformThumbnailUrl(e.select("media|thumbnail").attr("url"));
			news.content = processContent(e.select("content").html());
			
			res.add(news);
		}
		return res;
	}

	private static String transformThumbnailUrl(String url) {
		Pattern p = Pattern.compile("bbcimage.+images/");
		return p.matcher(url).replaceAll(IMAGE_BASE_URL);
	}

	private static String processContent(String content) {
		Pattern p = Pattern.compile("bbcimage.+images/");
		content = p.matcher(content).replaceAll(IMAGE_BASE_URL);
		return content;
	}



}


