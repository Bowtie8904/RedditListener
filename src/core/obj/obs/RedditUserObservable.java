package core.obj.obs;

import bt.remote.rest.REST;

/**
 * @author &#8904
 *
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
        return "https://oauth.reddit.com/user/" + getName() + "/submitted";
    }

    /**
     * @see RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/user/" + this.name + "/posts/";
    }
}