package core.obj;

import bt.remote.rest.REST;

/**
 * @author &#8904
 *
 */
public class RedditInbox extends RedditObservable
{
    /**
     * @param name
     */
    public RedditInbox(String name)
    {
        super(name);
        this.namePrefix = "";
    }

    /**
     * @see core.obj.RedditObservable#createRequestParameters()
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
    protected RedditThread createThread()
    {
        return new RedditMessage(this);
    }

    /**
     * @see core.obj.RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/message/inbox";
    }

    /**
     * @see core.obj.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/message/inbox/";
    }
}