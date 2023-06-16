package bt.redditlistener.reddit.observ;

import bt.redditlistener.reddit.notif.ModQueueMessageNotification;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.web.util.RestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModQueueObservable extends RedditObservable
{
    private Set<String> messages;

    /**
     * @param name
     */
    public ModQueueObservable(String name)
    {
        super(name);
        this.messages = new HashSet<>();
    }

    public Set<String> getMessages()
    {
        return this.messages;
    }

    public void addMessages(List<String> messages)
    {
        this.messages.addAll(messages);
    }

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
        return "https://oauth.reddit.com/r/" + getName() + "/about/modqueue";
    }

    /**
     * @see core.obj.obs.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/r/" + getName() + "/about/modqueue";
    }

    @Override
    protected void afterFiring(List<RedditNotification> notifications)
    {
        for (var msg : new HashSet<String>(this.messages))
        {
            if (!notifications.stream().anyMatch(n -> n.getId().equals(msg)))
            {
                this.messages.remove(msg);
            }
        }

        super.afterFiring(notifications);
    }

    @Override
    protected boolean shouldFireNewNotification(RedditNotification n)
    {
        boolean result = false;

        if (!this.messages.contains(n.getId()))
        {
            result = true;
            this.messages.add(n.getId());
        }

        return result;
    }

    @Override
    protected RedditNotification createNotification()
    {
        return new ModQueueMessageNotification(this);
    }

    @Override
    public String toString()
    {
        return "ModQueue r/" + getName();
    }
}