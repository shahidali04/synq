package synq_backend.user.dto;

import java.util.UUID;

// Data sent between the User API and frontend.
public class UserDTO {

    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String profileImageUrl;
    private String status;

    //no-argument constructor - allow an empty object to be created
    public UserDTO() {
    }

    //Getter and Setter Methods
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
