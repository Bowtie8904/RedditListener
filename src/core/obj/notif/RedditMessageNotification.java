package core.obj.notif;

import java.sql.Timestamp;

import core.obj.obs.RedditObservable;
import org.json.JSONObject;

/**
 * @author &#8904
 *
 */
public class RedditMessageNotification extends RedditNotification
{
    private String author;
    private String title;
    private String createdString;

    /**
     * @param obs
     */
    public RedditMessageNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;

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
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getCreatedString()
    {
        return createdString;
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