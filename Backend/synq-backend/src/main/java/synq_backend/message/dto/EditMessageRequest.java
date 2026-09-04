package synq_backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Represents the request body used to edit an existing message.
@Getter
@Setter
public class EditMessageRequest {

    @NotBlank( message = "Message content can't be empty")
    @Size(max = 5000, message = "Message content can't exceed 5000 characters")
    private String content;

}
