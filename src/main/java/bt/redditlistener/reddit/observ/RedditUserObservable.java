package bt.redditlistener.reddit.observ;

import bt.redditlistener.web.util.RestUtils;

/**
 * @author &#8904
 */
public class RedditUserObservable extends RedditObservable
{
    /**
     * @param name
     */
    public RedditUserObservable(String name)
    {
        super(name);
        this.namePrefix = "/u/";
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
        return "https://oauth.reddit.com/user/" + getName() + "/submitted";
    }

    /**
     * @see core.obj.obs.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/user/" + this.name + "/posts/";
    }
}