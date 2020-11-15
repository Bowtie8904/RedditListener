package core.db;

import java.util.List;

import bt.db.EmbeddedDatabase;
import bt.db.constants.Delete;
import bt.db.constants.Generated;
import bt.db.constants.SqlType;
import bt.db.listener.impl.IdentityListener;
import bt.db.statement.clause.Column;
import bt.db.statement.clause.foreign.ColumnForeignKey;
import core.obj.obs.*;

/**
 * @author &#8904
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
                .column(new Column("name", SqlType.VARCHAR).size(200))
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

        create().table("ModQueueMessage")
                .column(new Column("name", SqlType.VARCHAR).size(200).unique())
                .column(new Column("subreddit_id", SqlType.LONG).foreignKey(new ColumnForeignKey().references("RedditObservable", "id")
                                                                                                  .on(Delete.CASCADE)))
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
        List<RedditObservable> observables = select().from("RedditObservable")
                                                     .orderBy("name").asc()
                                                     .execute()
                                                     .map(result ->
                                                          {
                                                              RedditObservable obs = null;

                                                              Long id = result.getLong("id");
                                                              String name = result.getString("name");
                                                              Long lastThreadTimestamp = result.getLong("lastThreadTimestamp");
                                                              String type = result.getString("type");

                                                              switch (type.trim())
                                                              {
                                                                  case "user":
                                                                      obs = new RedditUserObservable(name);
                                                                      break;
                                                                  case "subreddit":
                                                                      obs = new SubredditObservable(name);
                                                                      break;
                                                                  case "inbox":
                                                                      obs = new RedditInboxObservable(name);
                                                                      break;
                                                                  case "modqueue":
                                                                      obs = new ModQueueObservable(name);
                                                                      break;
                                                              }

                                                              obs.setDbId(id);
                                                              obs.setLastThreadTimestamp(lastThreadTimestamp);

                                                              if (obs instanceof ModQueueObservable)
                                                              {
                                                                  ((ModQueueObservable)obs).addMessages(loadModQueueMessages((ModQueueObservable)obs));
                                                              }

                                                              return obs;
                                                          });

        return observables;
    }

    public List<String> loadModQueueMessages(ModQueueObservable queue)
    {
        return select().from("ModQueueMessage")
                       .where("subreddit_id").equal(queue.getDbId())
                       .unprepared()
                       .execute(true)
                       .map(rs -> rs.getString("name"));
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

            if (obs instanceof RedditUserObservable)
            {
                type = "user";
            }
            else if (obs instanceof ModQueueObservable)
            {
                type = "modqueue";
            }
            else if (obs instanceof SubredditObservable)
            {
                type = "subreddit";
            }
            else if (obs instanceof RedditInboxObservable)
            {
                type = "inbox";
            }

            insert().into("RedditObservable")
                    .set("name", obs.getName())
                    .set("lastThreadTimestamp", obs.getLastThreadTimestamp())
                    .set("type", type)
                    .unprepared()
                    .commit()
                    .execute(true);

            obs.setDbId(IdentityListener.getLast("RedditObservable"));
        }
        else
        {
            update("RedditObservable").set("name", obs.getName())
                                      .set("lastThreadId", obs.getLastId())
                                      .set("lastThreadTimestamp", obs.getLastThreadTimestamp())
                                      .where("id").equal(obs.getDbId())
                                      .unprepared()
                                      .commit()
                                      .execute(true);
        }

        if (obs instanceof ModQueueObservable)
        {
            saveModQueueMessages((ModQueueObservable)obs);
        }
    }

    public void saveModQueueMessages(ModQueueObservable queue)
    {
        delete().from("ModQueueMessage")
                .where("subreddit_id").equal(queue.getDbId())
                .unprepared()
                .execute(true);

        for (var msg : queue.getMessages())
        {
            insert().into("ModQueueMessage")
                    .set("name", msg)
                    .set("subreddit_id", queue.getDbId())
                    .unprepared()
                    .execute(true);
        }

        commit();
    }
}