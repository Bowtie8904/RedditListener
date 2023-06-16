package bt.redditlistener.reddit.observ;

import bt.redditlistener.config.Configuration;
import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.ObservableManager;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.reddit.notif.RedditThreadNotification;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author &#8904
 */
@Slf4j
public abstract class RedditObservable
{
    protected Long dbId;
    protected Configuration config;
    protected ObservableManager observableManager;
    protected String namePrefix;
    protected String name;
    protected String lastId;
    protected long nextRequest;
    protected List<Consumer<RedditNotification>> listeners;
    protected boolean isFirstRequest = true;
    protected long lastThreadTimestamp;
    protected boolean hidden;
    protected long deleteTimestamp = -1;
    protected DatabaseService databaseService;

    public RedditObservable(String name)
    {
        this.name = name;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    protected List<RedditNotification> extractNotifications(JSONArray json)
    {
        List<RedditNotification> notifications = new ArrayList<>(json.length());
        JSONObject notificationData = null;
        RedditNotification notification = null;

        for (int i = 0; i < json.length(); i++)
        {
            notificationData = json.getJSONObject(i);
            notification = createNotification();

            try
            {
                if (notification.parse(notificationData))
                {
                    notifications.add(0, notification);
                }
                else
                {
                    continue;
                }
            }
            catch (JSONException e)
            {
                log.error("Failed to parse notification for '" + this + "'.");
                log.error(notificationData.toString(4), e);
            }
        }

        return notifications;
    }

    protected JSONArray getChildrenArray(JSONObject json)
    {
        var data = json.getJSONObject("data");
        var children = data.getJSONArray("children");
        return children;
    }

    public void parseNewNotifications(JSONObject json)
    {
        try
        {
            var children = getChildrenArray(json);
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

            if (count > 0)
            {
                this.lastThreadTimestamp = lastTimestamp;
                log.debug(this + ": Found " + count + " new notifications.");

                // if this observable should be deleted after some time then extend its lifespan since its still active
                if (this.deleteTimestamp > -1)
                {
                    this.deleteTimestamp = this.lastThreadTimestamp + ((long)this.config.getHiddenThreadLifetimeHours() * 60 * 60 * 1000); // add configured hours to lifetime
                }
            }

            afterFiring(notifications);
        }
        catch (Exception e)
        {
            log.error("Failed to process notifications", e);
        }
    }

    protected void afterFiring(List<RedditNotification> notifications)
    {
        this.databaseService.save(this);
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
                count++;
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
        notification.persist(this.databaseService);

        if (!this.hidden)
        {
            for (var c : this.listeners)
            {
                c.accept(notification);
            }
        }
    }

    public synchronized void addListener(Consumer<RedditNotification> c)
    {
        this.listeners.add(c);
    }

    public void onDelete()
    {
        this.databaseService.delete(this);
    }

    /**
     * @return the lastThreadTimestamp
     */
    public long getLastThreadTimestamp()
    {
        return this.lastThreadTimestamp;
    }

    /**
     * @param lastThreadTimestamp the lastThreadTimestamp to set
     */
    public void setLastThreadTimestamp(long lastThreadTimestamp)
    {
        this.lastThreadTimestamp = lastThreadTimestamp;
    }

    public long getDeleteTimestamp()
    {
        return this.deleteTimestamp;
    }

    public void setDeleteTimestamp(long deleteTimestamp)
    {
        this.deleteTimestamp = deleteTimestamp;
    }

    /**
     * @return the dbId
     */
    public Long getDbId()
    {
        return this.dbId;
    }

    /**
     * @param dbId the dbId to set
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
     * @param config the config to set
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
     * @param nextRequest the nextRequest to set
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
     * @param name the name to set
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
     * @param lastId the lastId to set
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
     * @param namePrefix the namePrefix to set
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

    public boolean isHidden()
    {
        return this.hidden;
    }

    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public ObservableManager getObservableManager()
    {
        return this.observableManager;
    }

    public void setObservableManager(ObservableManager observableManager)
    {
        this.observableManager = observableManager;
    }

    public DatabaseService getDatabaseService()
    {
        return this.databaseService;
    }

    public void setDatabaseService(DatabaseService databaseService)
    {
        this.databaseService = databaseService;
    }

    public abstract String[] createRequestParameters();

    public abstract String getRequestUrl();

    public abstract String getLink();
}