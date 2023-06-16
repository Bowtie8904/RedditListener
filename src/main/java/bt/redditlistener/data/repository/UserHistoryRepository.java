package bt.redditlistener.data.repository;

import bt.redditlistener.data.entity.UserHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistoryEntity, Long>
{
}
