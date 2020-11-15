package core.obj.obs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import core.obj.notif.RedditNotification;
import core.obj.notif.RedditThreadNotification;
import org.json.JSONArray;
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
    protected List<Consumer<RedditNotification>> listeners;
    protected boolean isFirstRequest = true;
    protected long lastThreadTimestamp;

    public RedditObservable(String name)
    {
        this.name = name;
        this.lastThreadTimestamp = System.currentTimeMillis();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    protected List<RedditNotification> extractNotifications(JSONArray json)
    {
        List<RedditNotification> notifications = new ArrayList<>(json.length());
        JSONObject notificationData = null;
        RedditNotification notification = null;

        for (int i = 0; i < json.length(); i ++ )
        {
            notificationData = json.getJSONObject(i);
            notification = createNotification();
            notification.parse(notificationData);
            notifications.add(0, notification);
        }

        return notifications;
    }

    public void parseNewNotifications(JSONObject json)
    {
        try
        {
            var data = json.getJSONObject("data");
            var children = data.getJSONArray("children");
            long lastTimestamp = System.currentTimeMillis();
            List<RedditNotification> notifications = extractNotifications(children);

            notifications.sort(Comparator.comparing(RedditNotification::getCreated));

            if (notifications.size() > 0)
            {
                RedditNotification thread = notifications.get(notifications.size() - 1);
                this.lastId = thread.getId();
                lastTimestamp = thread.getCreated();
            }

            int count = firenewNotifications(notifications);

            this.lastThreadTimestamp = lastTimestamp;

            if (count > 0)
            {
                Logger.global().print(toString() + ": Found " + count + " new notifications.");
            }

            afterFiring(notifications);
        }
        catch (Exception e)
        {
            Logger.global().print(e);
        }
    }

    protected void afterFiring(List<RedditNotification> notifications)
    {
        this.config.getDatabase().save(this);
        this.isFirstRequest = false;
    }

    protected int firenewNotifications(List<RedditNotification> notifications)
    {
        int count = 0;

        for (var n : notifications)
        {
            if (shouldFireNewNotification(n))
            {
                fireNewNotification(n);
                count ++ ;
            }
        }

        return count;
    }

    protected boolean shouldFireNewNotification(RedditNotification n)
    {
        return n.getCreated() > this.lastThreadTimestamp;
    }

    protected RedditNotification createNotification()
    {
        return new RedditThreadNotification(this);
    }

    public synchronized void fireNewNotification(RedditNotification notification)
    {
        for (var c : this.listeners)
        {
            c.accept(notification);
        }
    }

    public synchronized void addListener(Consumer<RedditNotification> c)
    {
        this.listeners.add(c);
    }

    public void onDelete()
    {
        this.config.getDatabase().delete(this);
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