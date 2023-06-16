package bt.redditlistener.data.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Entity
@Data
@Table(name = "modqueuemessage")
public class ModQueueMessageEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modqueuemessage_SEQ")
    private Long id;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "subredditId", referencedColumnName = "id")
    private RedditObservableEntity subreddit;
}