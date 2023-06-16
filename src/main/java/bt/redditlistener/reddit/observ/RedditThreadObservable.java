package bt.redditlistener.reddit.observ;

import bt.redditlistener.reddit.notif.RedditCommentNotification;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.web.util.RestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedditThreadObservable extends RedditObservable
{
    private static final Pattern subredditPattern = Pattern.compile("/r/(.*?)/");
    private static final Pattern idPattern = Pattern.compile("/comments/(.*?)/");
    private static final Pattern namePattern = Pattern.compile("/comments/.*?/(.*?)/");

    private static final int MAX_TITLE_LENGTH = 28;

    private String id;
    private String subreddit;
    private Set<String> comments;
    private String threadName;

    /**
     * @param name
     */
    public RedditThreadObservable(String name, String id, String subreddit, String threadName)
    {
        super(name);
        this.namePrefix = "";
        this.id = id;
        this.subreddit = subreddit;
        this.threadName = threadName;

        this.comments = new HashSet<>();
    }

    public static RedditThreadObservable newFor(String link)
    {
        Matcher matcher = namePattern.matcher(link);
        matcher.find();
        String name = matcher.group(1).replace("_", " ");

        if (name.length() > MAX_TITLE_LENGTH)
        {
            name = name.substring(0, MAX_TITLE_LENGTH) + "...";
        }

        matcher = idPattern.matcher(link);
        matcher.find();
        String id = matcher.group(1);

        matcher = subredditPattern.matcher(link);
        matcher.find();
        String subreddit = matcher.group(1);

        return new RedditThreadObservable(link, id, subreddit, name);
    }

    private List<JSONObject> flattenCommentTree(JSONObject comment)
    {
        List<JSONObject> comments = new ArrayList<>();
        comments.add(comment);

        try
        {
            JSONObject commentData = comment.getJSONObject("data");
            JSONObject replies = commentData.getJSONObject("replies");
            JSONObject replyData = replies.getJSONObject("data");
            JSONArray children = replyData.getJSONArray("children");

            for (int i = 0; i < children.length(); i++)
            {
                JSONObject reply = children.getJSONObject(i);
                comments.addAll(flattenCommentTree(reply));
            }
        }
        catch (JSONException e)
        {
            // ignore. If no replies are present then this will be an empty string
        }

        return comments;
    }

    @Override
    protected JSONArray getChildrenArray(JSONObject json)
    {
        JSONArray returnArray = new JSONArray();
        JSONArray dataArray = json.getJSONArray("array");

        for (int i = 0; i < dataArray.length(); i++)
        {
            JSONObject element = dataArray.getJSONObject(i);
            JSONObject data = element.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

            for (int j = 0; j < children.length(); j++)
            {
                JSONObject comment = children.getJSONObject(j);
                String kind = comment.getString("kind");

                // t1 == comment
                // t3 == post
                if (kind.equalsIgnoreCase("t1"))
                {
                    List<JSONObject> commentList = flattenCommentTree(comment);

                    for (var com : commentList)
                    {
                        returnArray.put(com);
                    }
                }
            }
        }

        return returnArray;
    }

    @Override
    protected RedditNotification createNotification()
    {
        return new RedditCommentNotification(this);
    }

    /**
     * @see core.obj.obs.RedditObservable#createRequestParameters()
     */
    @Override
    public String[] createRequestParameters()
    {
        String[] params = new String[]
                {
                        RestUtils.formParam("show", "all"),
                        RestUtils.formParam("article", this.id)
                };

        return params;
    }

    /**
     * @see core.obj.obs.RedditObservable#getRequestUrl()
     */
    @Override
    public String getRequestUrl()
    {
        return "https://oauth.reddit.com/r/" + this.subreddit + "/comments/article";
    }

    /**
     * @see core.obj.obs.RedditObservable#getLink()
     */
    @Override
    public String getLink()
    {
        return "https://www.reddit.com/r/" + this.subreddit + "/comments/" + this.id;
    }

    public Set<String> getComments()
    {
        return this.comments;
    }

    public void addComments(List<String> comments)
    {
        this.comments.addAll(comments);
    }

    @Override
    public synchronized void fireNewNotification(RedditNotification notification)
    {
        // remove persist call

        notification.persist(this.databaseService);

        if (!this.hidden)
        {
            for (var c : this.listeners)
            {
                c.accept(notification);
            }
        }
    }

    @Override
    protected void afterFiring(List<RedditNotification> notifications)
    {
        for (var msg : new HashSet<String>(this.comments))
        {
            if (!notifications.stream().anyMatch(n -> n.getId().equals(msg)))
            {
                this.comments.remove(msg);
            }
        }

        super.afterFiring(notifications);
    }

    @Override
    protected boolean shouldFireNewNotification(RedditNotification n)
    {
        boolean result = false;

        if (n.getCreated() > this.lastThreadTimestamp && !this.comments.contains(n.getId()))
        {
            result = true;
            this.comments.add(n.getId());
        }

        return result;
    }

    @Override
    public String toString()
    {
        return "Thread '" + this.threadName + "'";
    }
}
