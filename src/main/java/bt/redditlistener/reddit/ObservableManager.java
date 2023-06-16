package bt.redditlistener.reddit;

import bt.redditlistener.config.Configuration;
import bt.redditlistener.data.DatabaseService;
import bt.redditlistener.reddit.observ.RedditObservable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author &#8904
 */
@Service
@Slf4j
public class ObservableManager
{
    private ObservableList<RedditObservable> observables;
    private List<RedditObservable> hiddenObservables;
    private Configuration config;
    private DatabaseService databaseService;

    public ObservableManager(Configuration config, DatabaseService databaseService)
    {
        this.config = config;
        this.observables = FXCollections.observableArrayList();
        this.hiddenObservables = new ArrayList<>();
        this.databaseService = databaseService;
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

    public void load()
    {
        for (var obs : this.databaseService.getObservables())
        {
            if (obs.isHidden())
            {
                addHidden(obs);
            }
            else
            {
                add(obs);
            }
        }
    }

    public ObservableList<RedditObservable> observables()
    {
        return this.observables;
    }

    public List<RedditObservable> hiddenObservables()
    {
        return this.hiddenObservables;
    }

    public void addHiddenObservable(RedditObservable obs)
    {
        this.databaseService.save(obs);
        addHidden(obs);
    }

    public void addObservable(RedditObservable obs)
    {
        this.databaseService.save(obs);
        add(obs);
    }

    private void add(RedditObservable obs)
    {
        obs.setDatabaseService(this.databaseService);
        obs.setConfig(this.config);
        obs.setObservableManager(this);
        this.observables.add(obs);
    }

    private void addHidden(RedditObservable obs)
    {
        obs.setDatabaseService(this.databaseService);
        obs.setConfig(this.config);
        obs.setObservableManager(this);
        this.hiddenObservables.add(obs);
    }

    public void removeObservable(RedditObservable obs)
    {
        this.observables.remove(obs);
    }

    public void removeHiddenObservable(RedditObservable obs)
    {
        this.hiddenObservables.remove(obs);
    }
}