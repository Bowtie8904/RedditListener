package bt.redditlistener.data.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Entity
@Data
@Table(name = "threadcomment")
public class ThreadCommentEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "threadcomment_SEQ")
    private Long id;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "threadId", referencedColumnName = "id")
    private RedditObservableEntity thread;
}