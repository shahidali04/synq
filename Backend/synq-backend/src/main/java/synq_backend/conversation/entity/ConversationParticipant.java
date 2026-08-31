package synq_backend.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import synq_backend.user.entity.User;

import java.time.LocalDateTime;

// Represents a user's participation in a conversation.
@Entity
@Table(
        name = "conversation_participants",
        // Prevents the same user from being added to the same conversation more than once.
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"conversation_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Links this participant record to a conversation.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // Links this participant record to a user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    // Creates a participant record with the conversation, user, and join timestamp.
    public ConversationParticipant(Conversation conversation,
                                   User user){
        this.conversation = conversation;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
    }
}
