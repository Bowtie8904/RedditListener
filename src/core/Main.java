package core;

import java.io.File;
import java.io.IOException;

import bt.gui.fx.core.instance.ScreenInstanceDispatcher;
import bt.log.Logger;
import core.config.Configuration;
import core.db.Database;
import core.msg.ApplicationStarted;
import core.obj.ModQueue;
import core.obj.ObservableManager;
import core.obj.RedditInbox;
import core.view.ScreenManager;
import core.web.RedditApplication;

/**
 * @author &#8904
 *
 */
public class Main
{
    public static void main(String[] args)
    {
        Logger.setMaxNumberOfFiles(1);
        Logger.setMaxFileSize(1000);
        Logger.global().hookSystemErr();
        Logger.global().hookSystemOut();

        var config = new Configuration();
        config.setObservableManager(new ObservableManager(config));
        config.setDatabase(new Database());
        config.load(new File("./config.xml"));

        try
        {
            config.getDatabase().setupQueryServer("RedditListener", config.getPort());
        }
        catch (IOException e)
        {
            Logger.global().print(e);
        }

        var app = new RedditApplication(config);

        ScreenInstanceDispatcher.get().subscribeTo(ScreenManager.class, screen ->
        {
            screen.setConfiguration(config);
        });

        ScreenInstanceDispatcher.get().subscribeTo(ApplicationStarted.class, msg ->
        {
            config.getObservableManager().load();

            boolean hasInbox = config.getObservableManager()
                                     .observables()
                                     .stream()
                                     .anyMatch(obs -> obs instanceof RedditInbox);

            if (!hasInbox)
            {
                config.getObservableManager().addObservable(new RedditInbox("Inbox"));
            }

            boolean hasMod = config.getObservableManager()
                                     .observables()
                                     .stream()
                                     .anyMatch(obs -> obs instanceof ModQueue);

            if (!hasMod)
            {
                config.getObservableManager().addObservable(new ModQueue());
            }

            app.startScheduler();
        });

        ScreenManager.main(args);
    }
}