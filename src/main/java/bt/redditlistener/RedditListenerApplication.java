package bt.redditlistener;

import bt.gui.fx.core.instance.ApplicationStarted;
import bt.gui.fx.core.instance.ScreenInstanceDispatcher;
import bt.redditlistener.config.Configuration;
import bt.redditlistener.reddit.ObservableManager;
import bt.redditlistener.reddit.RedditConnector;
import bt.redditlistener.reddit.observ.RedditInboxObservable;
import bt.redditlistener.view.ScreenManager;
import bt.scheduler.Threads;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class RedditListenerApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RedditListenerApplication.class, args);
    }

    public RedditListenerApplication(ApplicationContext context, Configuration config, ObservableManager observableManager, RedditConnector app)
    {
        System.setProperty("java.awt.headless", "false");

        ScreenInstanceDispatcher.get().subscribeTo(ScreenManager.class, manager ->
        {
            manager.setConfiguration(config);
            manager.setObservableManager(observableManager);
            manager.setContext(context);
            app.setHostServices(manager.getHostServices());
        });

        ScreenInstanceDispatcher.get().subscribeTo(ApplicationStarted.class, msg ->
        {
            observableManager.load();

            boolean hasInbox = observableManager.observables()
                                                .stream()
                                                .anyMatch(obs -> obs instanceof RedditInboxObservable);

            if (!hasInbox)
            {
                observableManager.addObservable(new RedditInboxObservable("Inbox"));
            }

            app.startScheduler();
        });

        Threads.get().execute(() -> ScreenManager.main(new String[0]));
    }
}