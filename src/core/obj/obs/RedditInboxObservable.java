package core.obj.obs;

import bt.remote.rest.REST;
import core.obj.notif.RedditMessageNotification;
import core.obj.notif.RedditNotification;

/**
 * @author &#8904
 *
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
     * @see RedditObservable#createRequestParameters()
     */
    @Override
    public String[] createRequestParameters()
    {
        String[] params = new String[]
        {
          REST.formParam("limit", this.config.getThreadsPerRequest() + ""), REST.formParam("show", "all")
        };

        return params;
    }

    @Override
    protected RedditNotification createNotification()
    {
        return new RedditMessageNotification(this);
    }

    /**
     * @see RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/message/inbox";
    }

    /**
     * @see RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/message/inbox/";
    }
}