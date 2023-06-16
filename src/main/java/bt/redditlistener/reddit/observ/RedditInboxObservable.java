package bt.redditlistener.reddit.observ;

import bt.redditlistener.reddit.notif.RedditMessageNotification;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.web.util.RestUtils;

/**
 * @author &#8904
 */
public class RedditInboxObservable extends RedditObservable
{
    /**
     * @param name
     */
    public RedditInboxObservable(String name)
    {
        super(name);
        this.namePrefix = "";
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

    @Override
    protected RedditNotification createNotification()
    {
        return new RedditMessageNotification(this);
    }

    /**
     * @see core.obj.obs.RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/message/inbox";
    }

    /**
     * @see core.obj.obs.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/message/inbox/";
    }
}