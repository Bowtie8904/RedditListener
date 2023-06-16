package bt.redditlistener.data.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Entity
@Data
@Table(name = "redditobservable")
public class RedditObservableEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "redditobservable_SEQ")
    private Long id;

    private String name;

    @Column(length = 20)
    private String lastThreadId;

    private long lastThreadTimestamp;

    private long deleteTimestamp;

    @Column(length = 20)
    private String type;

    private boolean hidden;
}