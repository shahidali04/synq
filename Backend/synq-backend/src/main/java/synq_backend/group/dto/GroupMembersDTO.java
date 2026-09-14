package synq_backend.group.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import synq_backend.conversation.entity.ParticipantRole;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GroupMembersDTO {

    private UUID userId;
    private String username;
    private String displayName;
    private ParticipantRole role;
}
