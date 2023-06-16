package bt.redditlistener.reddit.notif;

import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * @author &#8904
 */
public class RedditThreadNotification extends RedditNotification
{
    private String author;
    protected String title;
    protected String text;
    protected boolean isPinned;
    protected String createdString;

    public RedditThreadNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void persist(DatabaseService db)
    {
        db.addUserHistory(this.author, this.observable.getName(), "Thread", this.title, this.link, new Timestamp(getCreated()));
    }

    @Override
    public boolean parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.title = data.getString("title");
        this.text = data.getString("selftext");
        this.created = data.getLong("created_utc") * 1000;
        this.isPinned = data.getBoolean("pinned");
        this.author = data.getString("author");
        this.link = "https://www.reddit.com" + data.getString("permalink");
        this.createdString = new Timestamp(getCreated()).toString();

        return true;
    }

    /**
     * @return the isPinned
     */
    public boolean isPinned()
    {
        return this.isPinned;
    }

    /**
     * @param isPinned the isPinned to set
     */
    public void setPinned(boolean isPinned)
    {
        this.isPinned = isPinned;
    }

    /**
     * @return the text
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        this.text = text;
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