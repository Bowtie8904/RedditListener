package core.obj.notif;

import core.obj.obs.RedditObservable;
import org.json.JSONObject;

import java.sql.Timestamp;

public class RedditCommentNotification extends RedditNotification
{
    private String createdString;

    public RedditCommentNotification(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;
        this.link = "https://www.reddit.com" + data.getString("permalink");
        this.createdString = new Timestamp(getCreated()).toString();
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + "New comment in an observed thread.";
    }
}
