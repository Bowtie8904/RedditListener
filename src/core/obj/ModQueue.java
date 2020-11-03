package core.obj;

import bt.log.Logger;
import bt.remote.rest.REST;
import org.json.JSONObject;

import java.util.*;

public class ModQueue extends Subreddit
{
    private Set<String> messages;

    /**
     * @param name
     */
    public ModQueue(String name)
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
    public void parseNewThreads(JSONObject json)
    {
        try
        {
            var data = json.getJSONObject("data");

            var children = data.getJSONArray("children");

            List<RedditThread> threads = new ArrayList<>();

            JSONObject threadData = null;
            RedditThread thread = null;
            long lastTimestamp = System.currentTimeMillis();

            for (int i = 0; i < children.length(); i ++ )
            {
                threadData = children.getJSONObject(i);
                thread = createThread();
                thread.parse(threadData);
                threads.add(0, thread);
            }

            threads.sort(Comparator.comparing(RedditThread::getCreated).reversed());
            int lastThreadIndex = 0;

            for (int i = 0; i < threads.size(); i ++ )
            {
                thread = threads.get(i);

                if (i == lastThreadIndex)
                {
                    this.lastId = thread.getId();
                    lastTimestamp = thread.getCreated();
                }
            }

            threads.sort(Comparator.comparing(RedditThread::getCreated));

            int count = 0;

            for (var t : threads)
            {
                if (shouldFireNewThread(t))
                {
                    fireNewThread(t);
                    count ++ ;
                }
            }

            if (count > 0)
            {
                Logger.global().print(toString() + ": Found " + count + " new threads.");
            }

            for (var msg : this.messages)
            {
                if (!threads.stream().anyMatch(t -> t.getId().equals(msg)))
                {
                    this.messages.remove(msg);
                }
            }

            this.lastThreadTimestamp = lastTimestamp;
            this.config.getDatabase().save(this);

            this.isFirstRequest = false;
        }
        catch (Exception e)
        {
            Logger.global().print(e);
        }
    }

    @Override
    protected boolean shouldFireNewThread(RedditThread t)
    {
        boolean result = false;

        if (!this.messages.contains(t.getId()))
        {
            result = true;
            this.messages.add(t.getId());
        }

        return result;
    }

    @Override
    protected RedditThread createThread()
    {
        return new ModQueueMessage(this);
    }

    @Override
    public String toString()
    {
        return "ModQueue r/" + getName();
    }
}