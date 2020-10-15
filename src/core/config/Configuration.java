package core.config;

import java.io.File;

import org.dom4j.DocumentException;

import bt.io.xml.XML;
import bt.log.Logger;
import core.db.Database;
import core.obj.ObservableManager;

/**
 * @author &#8904
 *
 */
public class Configuration
{
    private Database database;
    private String userAgent;
    private String user;
    private String password;
    private String clientId;
    private String clientSecret;
    private int threadsPerRequest;
    private int requestInterval;
    private int x;
    private int y;
    private int port;
    private ObservableManager observableManager;

    public void load(File configFile)
    {
        try
        {
            var doc = XML.parse(configFile);
            this.user = doc.selectSingleNode("//" + XML.lowerNode("username")).getText();
            this.password = doc.selectSingleNode("//" + XML.lowerNode("password")).getText();
            this.clientId = doc.selectSingleNode("//" + XML.lowerNode("clientId")).getText();
            this.clientSecret = doc.selectSingleNode("//" + XML.lowerNode("clientSecret")).getText();
            this.userAgent = doc.selectSingleNode("//" + XML.lowerNode("userAgent")).getText();
            this.threadsPerRequest = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("threadsPerRequest")).getText());
            this.requestInterval = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("requestInterval")).getText());
            this.x = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("popUpX")).getText());
            this.y = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("popUpY")).getText());
            this.port = Integer.parseInt(doc.selectSingleNode("//" + XML.lowerNode("port")).getText());
        }
        catch (DocumentException e)
        {
            Logger.global().print(e);
        }
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the database
     */
    public Database getDatabase()
    {
        return this.database;
    }

    /**
     * @param database
     *            the database to set
     */
    public void setDatabase(Database database)
    {
        this.database = database;
    }

    /**
     * @return the x
     */
    public int getX()
    {
        return this.x;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY()
    {
        return this.y;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * @return the subRedditManager
     */
    public ObservableManager getObservableManager()
    {
        return this.observableManager;
    }

    /**
     * @param observableManager
     *            the subRedditManager to set
     */
    public void setObservableManager(ObservableManager observableManager)
    {
        this.observableManager = observableManager;
    }

    /**
     * @return the requestInterval
     */
    public int getRequestInterval()
    {
        return this.requestInterval;
    }

    /**
     * @param requestInterval
     *            the requestInterval to set
     */
    public void setRequestInterval(int requestInterval)
    {
        this.requestInterval = requestInterval;
    }

    /**
     * @return the threadsPerRequest
     */
    public int getThreadsPerRequest()
    {
        return this.threadsPerRequest;
    }

    /**
     * @param threadsPerRequest
     *            the threadsPerRequest to set
     */
    public void setThreadsPerRequest(int threadsPerRequest)
    {
        this.threadsPerRequest = threadsPerRequest;
    }

    /**
     * @return the userAgent
     */
    public String getUserAgent()
    {
        return this.userAgent;
    }

    /**
     * @param userAgent
     *            the userAgent to set
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * @return the user
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the clientId
     */
    public String getClientId()
    {
        return this.clientId;
    }

    /**
     * @param clientId
     *            the clientId to set
     */
    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret()
    {
        return this.clientSecret;
    }

    /**
     * @param clientSecret
     *            the clientSecret to set
     */
    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }
}