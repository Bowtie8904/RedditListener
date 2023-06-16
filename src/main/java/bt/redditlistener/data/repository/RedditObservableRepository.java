package bt.redditlistener.data.repository;

import bt.redditlistener.data.entity.RedditObservableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Repository
public interface RedditObservableRepository extends JpaRepository<RedditObservableEntity, Long>
{
}