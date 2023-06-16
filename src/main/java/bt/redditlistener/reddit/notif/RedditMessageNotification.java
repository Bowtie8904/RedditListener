package bt.redditlistener.reddit.notif;

import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * @author &#8904
 */
public class RedditMessageNotification extends RedditNotification
{
    private String author;
    private String title;
    private String createdString;
    private String text;
    private boolean wasComment;
    private String context;

    /**
     * @param obs
     */
    public RedditMessageNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void persist(DatabaseService db)
    {
        db.addUserHistory(this.author, this.subreddit, this.wasComment ? "Comment reply" : "Private message", this.text, this.context, new Timestamp(getCreated()));
    }

    @Override
    public boolean parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;

        this.text = data.getString("body");

        try
        {
            this.subreddit = data.getString("subreddit");
        }
        catch (Exception e)
        {
            this.subreddit = null;
        }

        this.context = data.getString("context");

        if (!this.context.startsWith("http"))
        {
            this.context = "https://www.reddit.com" + this.context;
        }

        try
        {
            this.wasComment = data.getBoolean("wasComment");
        }
        catch (Exception e)
        {
            this.wasComment = false;
        }

        try
        {
            this.author = data.getString("author");
        }
        catch (Exception e)
        {
            this.author = "System Message";
        }

        this.title = "Message from " + this.author;
        this.createdString = new Timestamp(getCreated()).toString();
        this.link = this.observable.getLink();

        return true;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCreatedString()
    {
        return this.createdString;
    }

    public void setCreatedString(String createdString)
    {
        this.createdString = createdString;
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + getTitle();
    }
}