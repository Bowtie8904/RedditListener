package bt.redditlistener.data.repository;

import bt.redditlistener.data.entity.ModQueueMessageEntity;
import bt.redditlistener.data.entity.RedditObservableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Repository
public interface ModQueueMessageRepository extends JpaRepository<ModQueueMessageEntity, Long>
{
    List<ModQueueMessageEntity> findAllBySubreddit(RedditObservableEntity subreddit);

    void deleteAllBySubreddit(RedditObservableEntity subreddit);
}