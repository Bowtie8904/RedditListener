package core.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HttpHeaders;

import bt.log.Logger;
import bt.remote.rest.REST;
import bt.scheduler.Threads;
import core.config.Configuration;
import core.obj.ObservableManager;
import core.obj.RedditObservable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * @author &#8904
 *
 */
public class RedditApplication
{
    public static DoubleProperty remainingRateLimit;

    private ObservableManager observableManager;
    private Configuration config;
    private RedditToken token;
    private volatile boolean canRequest = true;

    public RedditApplication(Configuration config)
    {
        this.config = config;
        this.observableManager = config.getObservableManager();
        remainingRateLimit = new SimpleDoubleProperty();
    }

    public void startScheduler()
    {
        Threads.get().scheduleWithFixedDelayDaemon(this::requestNewThreads,
                                                   1000,
                                                   this.config.getRequestInterval(),
                                                   TimeUnit.MILLISECONDS);
    }

    protected void checkToken()
    {
        if (this.token == null || !this.token.isValid())
        {
            try
            {
                requestToken();
            }
            catch (IOException e)
            {
                Logger.global().print(e);
            }
        }
    }

    public void requestToken() throws IOException
    {
        String auth = this.config.getClientId() + ":" + this.config.getClientSecret();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        Map<String, String> headers = new HashMap<>();

        headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        headers.put(HttpHeaders.USER_AGENT, this.config.getUserAgent());

        var json = REST.POST(RedditToken.REQUEST_ENDPOINT,
                             headers,
                             REST.formParam("grant_type", "password"),
                             REST.formParam("username", this.config.getUser()),
                             REST.formParam("password", this.config.getPassword()));

        this.token = new RedditToken(json);
    }

    private Map<String, String> createRequestHeaders()
    {
        checkToken();

        String authHeader = "Bearer " + this.token.getAccessToken();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, authHeader);
        headers.put(HttpHeaders.USER_AGENT, this.config.getUserAgent());

        return headers;
    }

    private void evaluateRateLimit(double used, double remaining, double reset)
    {
        remainingRateLimit.set(remaining);

        if (remaining == 0)
        {
            Logger.global().print("Rate limit reached. Next request allowed in " + reset + " seconds.");
            this.canRequest = false;

            Threads.get().scheduleDaemon(() ->
            {
                this.canRequest = true;
                Logger.global().print("Rate limit reset.");
            }, (int)reset + 5, TimeUnit.SECONDS);
        }
    }

    public synchronized void requestNewThreads()
    {
        try
        {
            List<RedditObservable> observables = new ArrayList<>(this.observableManager.observables());
            observables.sort(Comparator.comparing(RedditObservable::getNextRequest));
            requestNewThreads(observables);
        }
        catch (Exception e)
        {
            Logger.global().print(e);
        }
    }

    public synchronized void requestNewThreads(List<RedditObservable> obs)
    {
        boolean canContinue = true;

        for (var ob : obs)
        {
            canContinue = requestNewThreads(ob);

            if (!canContinue)
            {
                break;
            }
        }
    }

    public synchronized boolean requestNewThreads(RedditObservable obs)
    {
        boolean canContinue = true;

        if (this.canRequest)
        {
            if (System.currentTimeMillis() >= obs.getNextRequest())
            {
                var headers = createRequestHeaders();

                try
                {
                    var json = REST.GET(obs.getRequestUrl(), headers, obs.createRequestParameters());

                    obs.parseNewThreads(json);
                    obs.setNextRequest(System.currentTimeMillis() + this.config.getRequestInterval());

                    evaluateRateLimit(Double.parseDouble(headers.get("x-ratelimit-used")),
                                      Double.parseDouble(headers.get("x-ratelimit-remaining")),
                                      Double.parseDouble(headers.get("x-ratelimit-reset")));
                }
                catch (IOException e)
                {
                    Logger.global().print(e);
                }
            }
        }
        else
        {
            canContinue = false;
        }

        return canContinue;
    }
}