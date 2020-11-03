package core.obj;

import org.json.JSONObject;

import java.sql.Timestamp;

public class ModQueueMessage extends RedditThread
{
    /**
     * @param obs
     */
    public ModQueueMessage(RedditObservable obs)
    {
        super(obs);
    }

    @Override
    public void parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.created = data.getLong("created_utc") * 1000;

        this.title = "New item in ModQueue";
        this.createdString = new Timestamp(getCreated()).toString();
        this.link = "https://www.reddit.com" + data.getString("permalink");
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + getTitle();
    }
}