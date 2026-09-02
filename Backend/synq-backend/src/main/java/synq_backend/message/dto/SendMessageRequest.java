package synq_backend.message.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {

    private UUID conversationId;

    private String content;
}
