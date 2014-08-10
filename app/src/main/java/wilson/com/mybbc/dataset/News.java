package wilson.com.mybbc.dataset;

import android.os.Parcel;
import android.os.Parcelable;

public class News implements Parcelable {
		public String title;
		public String summary;
		public String category;
		public String updated;
		public String link;
		public String thumbnailUrl;
		public String content;

		public News() {}

		public News(String title, String summary, String category, String updated, String link,
		            String thumbnailUrl, String content) {
			this.title = title;
			this.summary = summary;
			this.category = category;
			this.updated = updated;
			this.link = link;
			this.thumbnailUrl = thumbnailUrl;
			this.content = content;
		}

		public static final Creator CREATOR = new Creator<News>() {
			@Override
			public News createFromParcel(Parcel source) {
				News News = new News();
				News.title = source.readString();
				News.summary = source.readString();
				News.category = source.readString();
				News.updated = source.readString();
				News.link = source.readString();
				News.thumbnailUrl = source.readString();
				News.content = source.readString();
				return News;
			}

			@Override
			public News[] newArray(int size) {
				return new News[size];
			}
		};

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(title);
			dest.writeString(summary);
			dest.writeString(category);
			dest.writeString(updated);
			dest.writeString(link);
			dest.writeString(thumbnailUrl);
			dest.writeString(content);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public String toString() {
			return "News{" +
					"title='" + title + '\'' +
					", summary='" + /*summary + */'\'' +
					", category='" + category + '\'' +
					", updated='" + updated + '\'' +
					", link='" + link + '\'' +
					", thumbnailUrl='" + thumbnailUrl + '\'' +
					", content='" + /*content + */'\'' +
					'}';
		}
	}
