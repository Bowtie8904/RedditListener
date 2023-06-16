package bt.redditlistener.reddit.notif;

import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

public class ModQueueMessageNotification extends RedditNotification
{
    private String createdString;
    private String title;
    private String author;
    private String info;

    /**
     * @param obs
     */
    public ModQueueMessageNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void persist(DatabaseService db)
    {
        db.addUserHistory(this.author, this.subreddit, "Report", this.info, this.link, new Timestamp(getCreated()));
    }

    @Override
    public boolean parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;

        this.author = data.getString("author");

        try
        {
            this.subreddit = data.getString("subreddit");
        }
        catch (Exception e)
        {
            this.subreddit = null;
        }

        try
        {
            this.info = data.getString("title");
        }
        catch (Exception e)
        {
            this.info = data.getString("body");
        }

        this.title = "New item in the queue";
        this.createdString = new Timestamp(getCreated()).toString();
        this.link = "https://www.reddit.com" + data.getString("permalink");

        return true;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + getTitle();
    }
}