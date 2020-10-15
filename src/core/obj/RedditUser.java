package core.obj;

import bt.remote.rest.REST;

/**
 * @author &#8904
 *
 */
public class RedditUser extends RedditObservable
{
    /**
     * @param name
     */
    public RedditUser(String name)
    {
        super(name);
        this.namePrefix = "/u/";
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

    /**
     * @see core.obj.RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/user/" + getName() + "/submitted";
    }

    /**
     * @see core.obj.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/user/" + this.name + "/posts/";
    }
}