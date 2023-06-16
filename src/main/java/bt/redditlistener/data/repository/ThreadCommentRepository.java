package bt.redditlistener.data.repository;

import bt.redditlistener.data.entity.RedditObservableEntity;
import bt.redditlistener.data.entity.ThreadCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Repository
public interface ThreadCommentRepository extends JpaRepository<ThreadCommentEntity, Long>
{
    List<ThreadCommentEntity> findAllByThread(RedditObservableEntity thread);

    void deleteAllByThread(RedditObservableEntity thread);
}