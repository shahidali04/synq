package synq_backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Represents the message payload sent by the client through WebSocket.
@Getter
@Setter
@NoArgsConstructor
public class WebSocketMessageRequest {

    @NotBlank
    private UUID conversationId;

    @NotBlank
    private String content;

}
