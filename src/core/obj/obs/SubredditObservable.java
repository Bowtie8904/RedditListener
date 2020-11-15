package core.obj.obs;

import bt.remote.rest.REST;
import core.obj.obs.RedditObservable;

/**
 * @author &#8904
 *
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

    /**
     * @see RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/r/" + getName() + "/new";
    }

    /**
     * @see RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/r/" + this.name + "/new/";
    }
}