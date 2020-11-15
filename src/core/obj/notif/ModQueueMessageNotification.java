package core.obj.notif;

import core.obj.obs.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

public class ModQueueMessageNotification extends RedditNotification
{
    private String createdString;
    private String title;

    /**
     * @param obs
     */
    public ModQueueMessageNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;

        this.title = "New item in the queue";
        this.createdString = new Timestamp(getCreated()).toString();
        this.link = "https://www.reddit.com" + data.getString("permalink");
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title
     *            the title to set
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