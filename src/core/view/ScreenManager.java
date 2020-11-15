package core.view;

import java.io.IOException;

import bt.gui.fx.core.FxScreenManager;
import bt.gui.fx.core.instance.ScreenInstanceDispatcher;
import bt.log.Logger;
import core.config.Configuration;
import core.msg.ApplicationStarted;
import core.obj.ObservableManager;
import core.obj.notif.RedditNotification;
import core.obj.obs.RedditObservable;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.stage.Stage;

/**
 * @author &#8904
 *
 */
public class ScreenManager extends FxScreenManager
{
    private ObservableManager observableManager;
    private Configuration config;

    public ScreenManager()
    {
    }

    public void setConfiguration(Configuration config)
    {
        this.config = config;

        ScreenInstanceDispatcher.get().subscribeTo(MainScreen.class, screen ->
        {
            screen.setObservableManager(config.getObservableManager());
        });

        this.observableManager = config.getObservableManager();
        this.observableManager.observables().addListener(this::onObservableChange);
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
                screen = new NewNotificationListScreen(getHostServices(), this.config.getX(), this.config.getY());
                addScreen(NewNotificationListScreen.class, screen);
                setScreen(NewNotificationListScreen.class, new Stage());
            }

            screen.addThread(thread);
        });
    }

    /**
     * @see bt.gui.fx.core.FxScreenManager#loadScreens()
     */
    @Override
    protected void loadScreens()
    {
    }

    /**
     * @see bt.gui.fx.core.FxScreenManager#startApplication()
     */
    @Override
    protected void startApplication()
    {
        try
        {
            MainScreen screen = new MainScreen(getHostServices());
            addScreen(MainScreen.class, screen);
            setScreen(MainScreen.class);
            screen.sendToSystemTray();
        }
        catch (IOException e)
        {
            Logger.global().print(e);
        }

        ScreenInstanceDispatcher.get().dispatch(new ApplicationStarted());
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}