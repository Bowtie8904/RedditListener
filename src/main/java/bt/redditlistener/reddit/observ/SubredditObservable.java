package bt.redditlistener.reddit.observ;

import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.web.util.RestUtils;

/**
 * @author &#8904
 */
public class SubredditObservable extends RedditObservable
{
    /**
     * @param name
     */
    public SubredditObservable(String name)
    {
        super(name);
        this.namePrefix = "/r/";
    }

    /**
     * @see core.obj.obs.RedditObservable#createRequestParameters()
     */
    @Override
    public String[] createRequestParameters()
    {
        String[] params = new String[]
                {
                        RestUtils.formParam("limit", this.config.getThreadsPerRequest() + ""), RestUtils.formParam("show", "all")
                };

        return params;
    }

    /**
     * @see core.obj.obs.RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/r/" + getName() + "/new";
    }

    /**
     * @see core.obj.obs.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/r/" + this.name + "/new/";
    }

    @Override
    public synchronized void fireNewNotification(RedditNotification notification)
    {
        super.fireNewNotification(notification);

        if (this.config.isTrackThreadComments())
        {
            var deleteTimestamp = notification.getCreated() + ((long)this.config.getHiddenThreadLifetimeHours() * 60 * 60 * 1000); // add configured hours to lifetime

            var threadObservable = RedditThreadObservable.newFor(notification.getLink());
            threadObservable.setHidden(true);
            threadObservable.setDeleteTimestamp(deleteTimestamp);

            this.observableManager.addHiddenObservable(threadObservable);
        }
    }
}