package core.obj;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.json.JSONObject;

import bt.log.Logger;
import core.config.Configuration;

/**
 * @author &#8904
 *
 */
public abstract class RedditObservable
{
    protected Long dbId;
    protected Configuration config;
    protected String namePrefix;
    protected String name;
    protected String lastId;
    protected long nextRequest;
    protected List<Consumer<RedditThread>> listeners;
    protected boolean isFirstRequest = true;
    protected long lastThreadTimestamp;

    public RedditObservable(String name)
    {
        this.name = name;
        this.lastThreadTimestamp = System.currentTimeMillis();
        this.listeners = new CopyOnWriteArrayList<>();
    }

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
                if (t.getCreated() > this.lastThreadTimestamp)
                {
                    fireNewThread(t);
                    count ++ ;
                }
            }

            if (count > 0)
            {
                Logger.global().print(toString() + ": Found " + count + " new threads.");
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

    protected RedditThread createThread()
    {
        return new RedditThread(this);
    }

    public synchronized void fireNewThread(RedditThread thread)
    {
        for (var c : this.listeners)
        {
            c.accept(thread);
        }
    }

    public synchronized void addListener(Consumer<RedditThread> c)
    {
        this.listeners.add(c);
    }

    /**
     * @return the lastThreadTimestamp
     */
    public long getLastThreadTimestamp()
    {
        return this.lastThreadTimestamp;
    }

    /**
     * @param lastThreadTimestamp
     *            the lastThreadTimestamp to set
     */
    public void setLastThreadTimestamp(long lastThreadTimestamp)
    {
        this.lastThreadTimestamp = lastThreadTimestamp;
    }

    /**
     * @return the dbId
     */
    public Long getDbId()
    {
        return this.dbId;
    }

    /**
     * @param dbId
     *            the dbId to set
     */
    public void setDbId(Long dbId)
    {
        this.dbId = dbId;
    }

    /**
     * @return the config
     */
    public Configuration getConfig()
    {
        return this.config;
    }

    /**
     * @param config
     *            the config to set
     */
    public void setConfig(Configuration config)
    {
        this.config = config;
    }

    /**
     * @return the nextRequest
     */
    public long getNextRequest()
    {
        return this.nextRequest;
    }

    /**
     * @param nextRequest
     *            the nextRequest to set
     */
    public void setNextRequest(long nextRequest)
    {
        this.nextRequest = nextRequest;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the lastId
     */
    public String getLastId()
    {
        return this.lastId;
    }

    /**
     * @param lastId
     *            the lastId to set
     */
    public void setLastId(String lastId)
    {
        this.lastId = lastId;
    }

    /**
     * @return the namePrefix
     */
    public String getNamePrefix()
    {
        return this.namePrefix;
    }

    /**
     * @param namePrefix
     *            the namePrefix to set
     */
    public void setNamePrefix(String namePrefix)
    {
        this.namePrefix = namePrefix;
    }

    @Override
    public String toString()
    {
        return getNamePrefix() + getName();
    }

    public abstract String[] createRequestParameters();

    public abstract String getRequestUrl();

    public abstract String getLink();
}