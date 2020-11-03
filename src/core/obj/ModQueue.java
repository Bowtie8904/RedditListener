package core.obj;

import bt.remote.rest.REST;

public class ModQueue extends Subreddit
{
    /**
     * @param name
     */
    public ModQueue()
    {
        super("mod");
    }

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
        return "https://oauth.reddit.com/r/" + getName() + "/about/modqueue";
    }

    /**
     * @see core.obj.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/r/" + getName() + "/about/modqueue";
    }

    @Override
    protected RedditThread createThread()
    {
        return new ModQueueMessage(this);
    }

    @Override
    public String toString()
    {
        return "ModQueue";
    }
}