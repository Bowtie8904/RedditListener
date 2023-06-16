package bt.redditlistener.reddit.notif;

import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

public class RedditCommentNotification extends RedditNotification
{
    private String createdString;
    private String author;
    private String text;
    private long edited;

    public RedditCommentNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void persist(DatabaseService db)
    {
        db.addUserHistory(this.author, this.subreddit, "Comment", this.text, this.link, new Timestamp(getCreated()));
    }

    @Override
    public boolean parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        if (data.has("body"))
        {
            this.text = data.getString("body");

            if (data.has("subreddit"))
            {
                this.subreddit = data.getString("subreddit");
            }

            if (data.has("author"))
            {
                this.author = data.getString("author");
            }
            else
            {
                this.author = "System Message";
            }

            try
            {
                this.edited = data.getLong("edited");
            }
            catch (Exception e)
            {
                this.edited = 0;
            }

            this.id = data.getString("name");
            this.created = data.getLong("created_utc") * 1000;
            this.link = "https://www.reddit.com" + data.getString("permalink");
            this.createdString = new Timestamp(getCreated()).toString();

            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + this.text.substring(0, Math.min(60, this.text.length())).replace("\n", " ");
    }
}
