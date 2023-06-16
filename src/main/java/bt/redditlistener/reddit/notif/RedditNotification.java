package bt.redditlistener.reddit.notif;

import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import org.json.JSONObject;

public abstract class RedditNotification
{
    protected RedditObservable observable;
    protected long created;
    protected String id;
    protected String link;
    protected String subreddit;

    public RedditNotification(RedditObservable obs)
    {
        this.observable = obs;
    }

    /**
     * @return the created
     */
    public long getCreated()
    {
        return this.created;
    }

    /**
     * @param created the created to set
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
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the link
     */
    public String getLink()
    {
        return this.link;
    }

    /**
     * @param link the link to set
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

    public abstract void persist(DatabaseService db);

    public abstract boolean parse(JSONObject json);
}
