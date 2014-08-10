package wilson.com.mybbc;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wilson.com.mybbc.dataset.News;


public class NewsDetailFragment extends Fragment {
	private static final String TAG = "NewsDetailFragment";
	private static final String KEY_NEWS_URL = "news_url";
	private static final String KEY_NEWS_TITLE = "news_title";
	private static final String KEY_NEWS_CONTENT = "news_content";

	public static NewsDetailFragment newInstance(News item) {
		NewsDetailFragment fragment = new NewsDetailFragment();
		Bundle args = new Bundle();
		args.putString(KEY_NEWS_TITLE, item.title);
		args.putString(KEY_NEWS_CONTENT, item.content);
		fragment.setArguments(args);
		return fragment;
	}

    public NewsDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);
	    final WebView webView = (WebView) rootView.findViewById(R.id.webView);

	    String html = getNewsHtml(getArguments().getString(KEY_NEWS_TITLE),
			    getArguments().getString(KEY_NEWS_CONTENT));
		webView.loadData(html, "text/html", "UTF-8");

	    return rootView;
    }

	private String getNewsHtml(String title, String content) {
		if (TextUtils.isEmpty(content)) {
			return "";
		}

		int marginLarge = 12;
		int marginSmall = 4;
		int marginExtraSmall = 2;
		int fullSizeImageWidth = 250;

		final String linkColor = "1e8cbe";
		final String greyLight = "#dddddd";
		final String greyExtraLight = "#eeeeee";

		StringBuilder sbHtml = new StringBuilder("<!DOCTYPE html><html><head><meta charset='UTF-8' />");

		// title isn't strictly necessary, but source is invalid html5 without one
		sbHtml.append("<title>News Detail</title>");

		// https://developers.google.com/chrome/mobile/docs/webview/pixelperfect
		sbHtml.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");

		sbHtml.append("<style type='text/css'>")
				.append("  body, p, div { max-width: 100% !important; word-wrap: break-word; }")
				.append("  p, div { line-height: 1.6em; font-size: 1em; }")
				.append("  h1, h2 { line-height: 1.2em; }");

		// make sure long strings don't force the user to scroll horizontally
		sbHtml.append("  body, p, div, a { word-wrap: break-word; }");

		// use a consistent top/bottom margin for paragraphs, with no top margin for the first one
		sbHtml.append(String.format("  p { margin-top: %dpx; margin-bottom: %dpx; }", marginSmall, marginSmall))
				.append("    p:first-child { margin-top: 0px; }");

		// add border, background color, and padding to pre blocks, and add overflow scrolling
		// so user can scroll the block if it's wider than the display
		sbHtml.append("  pre { overflow-x: scroll;")
				.append("        border: 1px solid ").append(greyLight).append("; ")
				.append("        background-color: ").append(greyExtraLight).append("; ")
				.append("        padding: ").append(marginSmall).append("px; }");

		// add a left border to blockquotes
		sbHtml.append("  blockquote { margin-left: ").append(marginSmall).append("px; ")
				.append("               padding-left: ").append(marginSmall).append("px; ")
				.append("               border-left: 3px solid ").append(greyLight).append("; }");

		// show links in the same color they are elsewhere in the app
		sbHtml.append("  a { text-decoration: none; color: ").append(linkColor).append("; }");

		// if javascript is allowed, make sure embedded videos fit the browser width and
		// use 16:9 ratio (YouTube standard) - if not allowed, hide iframes/embeds
		int videoWidth = 250;
		int videoHeight = (int) (videoWidth * 0.5625f);
		sbHtml.append("  iframe, embed { width: ").append(videoWidth).append("px !important;")
				.append("                  height: ").append(videoHeight).append("px !important; }");

		// don't allow any image to be wider than the screen
		sbHtml.append("  img { max-width: 100% !important; height: auto;}");

		// show large wp images full-width (unnecessary in most cases since they'll already be at least
		// as wide as the display, except maybe when viewed on a large landscape tablet)
		sbHtml.append("  img.size-full, img.size-large { display: block; width: 100% !important; height: auto; }");

		// center medium-sized wp image
		sbHtml.append("  img.size-medium { display: block; margin-left: auto !important; margin-right: auto !important; }");

		sbHtml.append("</style><h2>" + title + "</h2></head><body>")
				.append(content)
				.append("</body></html>");

		return sbHtml.toString();
	}

}
