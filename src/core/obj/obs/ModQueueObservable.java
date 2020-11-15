package core.obj.obs;

import bt.remote.rest.REST;
import core.obj.notif.ModQueueMessageNotification;
import core.obj.notif.RedditNotification;

import java.util.*;

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
        return messages;
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
        return "https://oauth.reddit.com/r/" + getName() + "/about/modqueue";
    }

    /**
     * @see RedditObservable#getLink()
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