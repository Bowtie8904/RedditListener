package bt.redditlistener.data.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author Lukas Hartwig
 * @since 11.06.2023
 */
@Entity
@Data
@Table(name = "userhistory")
public class UserHistoryEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userhistory_SEQ")
    private Long id;

    private Timestamp timestamp;

    private String name;

    private String subreddit;

    private String action;

    @Column(length = 500)
    private String link;

    @Lob
    private String text;
}