package bt.redditlistener.data;

import bt.redditlistener.data.entity.ModQueueMessageEntity;
import bt.redditlistener.data.entity.RedditObservableEntity;
import bt.redditlistener.data.entity.ThreadCommentEntity;
import bt.redditlistener.data.entity.UserHistoryEntity;
import bt.redditlistener.data.repository.ModQueueMessageRepository;
import bt.redditlistener.data.repository.RedditObservableRepository;
import bt.redditlistener.data.repository.ThreadCommentRepository;
import bt.redditlistener.data.repository.UserHistoryRepository;
import bt.redditlistener.reddit.observ.*;
import bt.utils.Null;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 10.06.2023
 */
@Service
public class DatabaseService
{
    private ModQueueMessageRepository modQueueMessageRepository;
    private RedditObservableRepository redditObservableRepository;
    private UserHistoryRepository userHistoryRepository;
    private ThreadCommentRepository threadCommentRepository;

    public DatabaseService(ModQueueMessageRepository modQueueMessageRepository,
                           RedditObservableRepository redditObservableRepository,
                           UserHistoryRepository userHistoryRepository,
                           ThreadCommentRepository threadCommentRepository)
    {
        this.modQueueMessageRepository = modQueueMessageRepository;
        this.redditObservableRepository = redditObservableRepository;
        this.userHistoryRepository = userHistoryRepository;
        this.threadCommentRepository = threadCommentRepository;
    }

    public List<RedditObservable> getObservables()
    {
        return this.redditObservableRepository.findAll(Sort.by(Sort.Order.asc("name")))
                                              .stream()
                                              .map(entity -> {
                                                  RedditObservable obs = null;

                                                  switch (entity.getType())
                                                  {
                                                      case "user":
                                                          obs = new RedditUserObservable(entity.getName());
                                                          break;
                                                      case "subreddit":
                                                          obs = new SubredditObservable(entity.getName());
                                                          break;
                                                      case "inbox":
                                                          obs = new RedditInboxObservable(entity.getName());
                                                          break;
                                                      case "modqueue":
                                                          obs = new ModQueueObservable(entity.getName());
                                                          break;
                                                      case "thread":
                                                          obs = RedditThreadObservable.newFor(entity.getName());
                                                          break;
                                                  }

                                                  obs.setDbId(entity.getId());
                                                  obs.setLastThreadTimestamp(entity.getLastThreadTimestamp());
                                                  obs.setHidden(entity.isHidden());
                                                  obs.setDeleteTimestamp(entity.getDeleteTimestamp());

                                                  if (obs instanceof ModQueueObservable)
                                                  {
                                                      ((ModQueueObservable)obs).addMessages(getModQueueMessages(entity));
                                                  }
                                                  else if (obs instanceof RedditThreadObservable)
                                                  {
                                                      ((RedditThreadObservable)obs).addComments(getThreadComments(entity));
                                                  }

                                                  return obs;
                                              })
                                              .collect(Collectors.toList());
    }

    private List<String> getModQueueMessages(RedditObservableEntity observableEntity)
    {
        return this.modQueueMessageRepository.findAllBySubreddit(observableEntity)
                                             .stream()
                                             .map(ModQueueMessageEntity::getName)
                                             .toList();
    }

    private List<String> getThreadComments(RedditObservableEntity observableEntity)
    {
        return this.threadCommentRepository.findAllByThread(observableEntity)
                                           .stream()
                                           .map(ThreadCommentEntity::getName)
                                           .toList();
    }

    @Transactional
    public void save(RedditObservable obs)
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
        else if (obs instanceof RedditThreadObservable)
        {
            type = "thread";
        }

        RedditObservableEntity entity = new RedditObservableEntity();
        entity.setName(obs.getName());
        entity.setType(type);
        entity.setLastThreadTimestamp(obs.getLastThreadTimestamp());
        entity.setDeleteTimestamp(obs.getDeleteTimestamp());
        entity.setHidden(obs.isHidden());

        if (obs.getDbId() == null)
        {
            entity = this.redditObservableRepository.save(entity);

            obs.setDbId(entity.getId());
        }
        else
        {
            entity.setId(obs.getDbId());
            entity.setLastThreadId(obs.getLastId());
            entity = this.redditObservableRepository.save(entity);
        }

        if (obs instanceof ModQueueObservable)
        {
            saveModQueueMessages((ModQueueObservable)obs, entity);
        }
        else if (obs instanceof RedditThreadObservable)
        {
            saveThreadComments((RedditThreadObservable)obs, entity);
        }
    }

    public void saveModQueueMessages(ModQueueObservable queue, RedditObservableEntity observableEntity)
    {
        this.modQueueMessageRepository.deleteAllBySubreddit(observableEntity);

        this.modQueueMessageRepository.saveAll(queue.getMessages()
                                                    .stream()
                                                    .map(msg -> {
                                                        ModQueueMessageEntity entity = new ModQueueMessageEntity();
                                                        entity.setName(msg);
                                                        entity.setSubreddit(observableEntity);
                                                        return entity;
                                                    })
                                                    .toList());
    }

    public void saveThreadComments(RedditThreadObservable thread, RedditObservableEntity observableEntity)
    {
        this.threadCommentRepository.deleteAllByThread(observableEntity);

        this.threadCommentRepository.saveAll(thread.getComments()
                                                   .stream()
                                                   .map(msg -> {
                                                       ThreadCommentEntity entity = new ThreadCommentEntity();
                                                       entity.setName(msg);
                                                       entity.setThread(observableEntity);
                                                       return entity;
                                                   })
                                                   .toList());
    }

    @Transactional
    public void delete(RedditObservable obs)
    {
        if (obs.getDbId() != null)
        {
            var entity = this.redditObservableRepository.findById(obs.getDbId());

            if (entity.isPresent())
            {
                this.threadCommentRepository.deleteAllByThread(entity.get());
                this.modQueueMessageRepository.deleteAllBySubreddit(entity.get());
            }

            this.redditObservableRepository.deleteById(obs.getDbId());
        }
    }

    public void addUserHistory(String username, String subreddit, String action, String text, String link, Timestamp timestamp)
    {
        text = Null.nullValue(text, "");

        if (text.length() > 9999)
        {
            text = text.substring(0, 9995) + "...";
        }

        subreddit = Null.nullValue(subreddit, "").toLowerCase();

        UserHistoryEntity entity = new UserHistoryEntity();
        entity.setName(username);
        entity.setSubreddit(subreddit);
        entity.setAction(action);
        entity.setText(text);
        entity.setLink(link);
        entity.setTimestamp(timestamp);

        this.userHistoryRepository.save(entity);
    }
}