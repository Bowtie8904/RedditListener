package core.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bt.db.EmbeddedDatabase;
import bt.db.constants.Generated;
import bt.db.constants.SqlType;
import bt.db.listener.impl.IdentityListener;
import bt.db.statement.clause.Column;
import bt.log.Logger;
import core.obj.RedditInbox;
import core.obj.RedditObservable;
import core.obj.RedditUser;
import core.obj.Subreddit;

/**
 * @author &#8904
 *
 */
public class Database extends EmbeddedDatabase
{
    /**
     * @see bt.db.DatabaseAccess#createTables()
     */
    @Override
    protected void createTables()
    {
        create().table("RedditObservable")
                .column(new Column("id", SqlType.LONG).primaryKey().generated(Generated.ALWAYS).asIdentity())
                .column(new Column("name", SqlType.VARCHAR).size(200).unique())
                .column(new Column("lastThreadId", SqlType.VARCHAR).size(20))
                .column(new Column("lastThreadTimestamp", SqlType.LONG))
                .column(new Column("type", SqlType.VARCHAR).size(20))
                .createDefaultUpdateTrigger(false)
                .createDefaultDeleteTrigger(false)
                .onAlreadyExists((stmt, e) ->
                {
                    System.out.println("Table " + stmt.getName() + " already exists.");
                    System.out.println("Execution time: " + stmt.getExecutionTime());
                    return 0;
                })
                .onSuccess((stmt, e) ->
                {
                    System.out.println("Created table " + stmt.getName() + ".");
                    System.out.println("Execution time: " + stmt.getExecutionTime());
                })
                .execute();
    }

    public List<RedditObservable> load()
    {
        List<RedditObservable> observables = new ArrayList<>();

        select().from("RedditObservable")
                .orderBy("name").asc()
                .executeAsStream()
                .stream()
                .forEach(result ->
                {
                    try
                    {
                        RedditObservable obs = null;

                        Long id = result.getLong("id");
                        String name = result.getString("name");
                        Long lastThreadTimestamp = result.getLong("lastThreadTimestamp");
                        String type = result.getString("type");

                        switch (type.trim())
                        {
                            case "user":
                                obs = new RedditUser(name);
                                break;
                            case "subreddit":
                                obs = new Subreddit(name);
                                break;
                            case "inbox":
                                obs = new RedditInbox(name);
                                break;
                        }

                        obs.setDbId(id);
                        obs.setLastThreadTimestamp(lastThreadTimestamp);

                        observables.add(obs);
                    }
                    catch (SQLException e)
                    {
                        Logger.global().print(e);
                    }
                });

        return observables;
    }

    public void delete(RedditObservable obs)
    {
        delete().from("RedditObservable")
                .where("id").equal(obs.getDbId() != null ? obs.getDbId() : -1)
                .commit()
                .execute();
    }

    public void save(RedditObservable obs)
    {
        if (obs.getDbId() == null)
        {
            String type = "";

            if (obs instanceof RedditUser)
            {
                type = "user";
            }
            else if (obs instanceof Subreddit)
            {
                type = "subreddit";
            }
            else if (obs instanceof RedditInbox)
            {
                type = "inbox";
            }

            insert().into("RedditObservable")
                    .set("name", obs.getName())
                    .set("lastThreadTimestamp", obs.getLastThreadTimestamp())
                    .set("type", type)
                    .commit()
                    .execute();

            obs.setDbId(IdentityListener.getLast("RedditObservable"));
        }
        else
        {
            update("RedditObservable").set("name", obs.getName())
                                      .set("lastThreadId", obs.getLastId())
                                      .set("lastThreadTimestamp", obs.getLastThreadTimestamp())
                                      .where("id").equal(obs.getDbId())
                                      .commit()
                                      .execute();
        }
    }
}