package core.obj;

import java.sql.Timestamp;

import org.json.JSONObject;

/**
 * @author &#8904
 *
 */
public class RedditMessage extends RedditThread
{
    private String author;

    /**
     * @param obs
     */
    public RedditMessage(RedditObservable obs)
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

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + getTitle();
    }
}