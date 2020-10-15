package core.obj;

import java.sql.Timestamp;

import org.json.JSONObject;

/**
 * @author &#8904
 *
 */
public class RedditThread
{
    protected RedditObservable observable;
    protected long created;
    protected String id;
    protected String title;
    protected String text;
    protected String link;
    protected boolean isPinned;
    protected String createdString;

    public RedditThread(RedditObservable sub)
    {
        this.observable = sub;
    }

    public void parse(JSONObject json)
    {
        var data = json.getJSONObject("data");

        this.id = data.getString("name");
        this.title = data.getString("title");
        this.text = data.getString("selftext");
        this.created = data.getLong("created_utc") * 1000;
        this.isPinned = data.getBoolean("pinned");
        this.link = "https://www.reddit.com" + data.getString("permalink");
        this.createdString = new Timestamp(getCreated()).toString();
    }

    /**
     * @return the isPinned
     */
    public boolean isPinned()
    {
        return this.isPinned;
    }

    /**
     * @param isPinned
     *            the isPinned to set
     */
    public void setPinned(boolean isPinned)
    {
        this.isPinned = isPinned;
    }

    /**
     * @return the link
     */
    public String getLink()
    {
        return this.link;
    }

    /**
     * @param link
     *            the link to set
     */
    public void setLink(String link)
    {
        this.link = link;
    }

    /**
     * @return the subreddit
     */
    public RedditObservable getObservable()
    {
        return this.observable;
    }

    /**
     * @return the text
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * @param text
     *            the text to set
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
     * @param title
     *            the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the created
     */
    public long getCreated()
    {
        return this.created;
    }

    /**
     * @param created
     *            the created to set
     */
    public void setCreated(long created)
    {
        this.created = created;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return this.observable.toString() + "   " + this.createdString + "\n" + getTitle();
    }
}