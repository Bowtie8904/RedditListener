package bt.redditlistener.view;

import bt.gui.fx.core.FxScreenManager;
import bt.gui.fx.core.instance.ScreenInstanceDispatcher;
import bt.redditlistener.config.Configuration;
import bt.redditlistener.reddit.ObservableManager;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.reddit.observ.RedditObservable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

/**
 * @author &#8904
 */
public class ScreenManager extends FxScreenManager
{
    private ObservableManager observableManager;
    private Configuration config;
    private ApplicationContext context;

    public ScreenManager()
    {
    }

    public static void main(String[] args)
    {
        Application.launch(args);
    }

    public void setConfiguration(Configuration config)
    {
        this.config = config;
    }

    public void setObservableManager(ObservableManager observableManager)
    {
        ScreenInstanceDispatcher.get().subscribeTo(MainScreen.class, screen ->
        {
            screen.setObservableManager(observableManager);
        });

        this.observableManager = observableManager;
        this.observableManager.observables().addListener(this::onObservableChange);
    }

    public void setContext(ApplicationContext context)
    {
        this.context = context;
    }

    private void onObservableChange(Change<? extends RedditObservable> c)
    {
        while (c.next())
        {
            for (var obs : c.getAddedSubList())
            {
                obs.addListener(this::onNewThread);
            }
        }
    }

    public synchronized void onNewThread(RedditNotification thread)
    {
        Platform.runLater(() ->
                          {
                              var screen = getScreen(NewNotificationListScreen.class, false);

                              if (screen == null || !screen.isActive())
                              {
                                  screen = this.context.getBean(NewNotificationListScreen.class);
                                  screen.setHostServices(getHostServices());
                                  screen.setX(this.config.getX());
                                  screen.setY(this.config.getY());
                                  addScreen(NewNotificationListScreen.class, screen);
                                  setScreen(NewNotificationListScreen.class, new Stage());
                              }

                              screen.addThread(thread);
                          });
    }

    /**
     * @see FxScreenManager#loadScreens()
     */
    @Override
    protected void loadScreens()
    {
    }

    /**
     * @see FxScreenManager#startApplication()
     */
    @Override
    protected void startApplication()
    {
        MainScreen screen = this.context.getBean(MainScreen.class);
        screen.setHostServices(getHostServices());
        addScreen(MainScreen.class, screen);
        setScreen(MainScreen.class);
        screen.sendToSystemTray();
        screen.getObservableManager().observables().addListener(screen::onObservableChange);
    }
}