package core.obj;

import core.config.Configuration;
import core.obj.obs.RedditObservable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author &#8904
 *
 */
public class ObservableManager
{
    private ObservableList<RedditObservable> observables;
    private Configuration config;

    public ObservableManager(Configuration config)
    {
        this.config = config;
        this.observables = FXCollections.observableArrayList();
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

    public void load()
    {
        for (var obs : this.config.getDatabase().load())
        {
            add(obs);
        }
    }

    public ObservableList<RedditObservable> observables()
    {
        return this.observables;
    }

    public void addObservable(RedditObservable obs)
    {
        this.config.getDatabase().save(obs);
        add(obs);
    }

    private void add(RedditObservable obs)
    {
        obs.setConfig(this.config);
        this.observables.add(obs);
    }

    public void removeObservable(RedditObservable obs)
    {
        this.observables.remove(obs);
    }
}