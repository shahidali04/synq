package synq_backend.group.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import synq_backend.group.dto.CreateGroupRequest;
import synq_backend.group.dto.GroupDTO;
import synq_backend.group.dto.GroupMembersDTO;
import synq_backend.group.service.GroupService;
import synq_backend.user.entity.User;
import synq_backend.group.dto.AddGroupMemberRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    // Creates a new group and makes the authenticated user its OWNER.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDTO createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication
    ){
        User currentUser = (User) authentication.getPrincipal();

        return groupService.createGroup(
                currentUser.getId(),
                request
        );
    }

    // Adds a new member to the group. Only the group OWNER can perform this action.
    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        groupService.addMember(
                groupId,
                currentUser.getId(),
                request
        );
    }

    // Retrieves all members belonging to the specified group.
    @GetMapping("/{groupId}/members")
    public List<GroupMembersDTO> getGroupMembers(
            @PathVariable UUID groupId
    ) {
        return groupService.getGroupMembers(groupId);
    }

    // Removes a member from the group. Only the group OWNER can perform this action.
    @DeleteMapping("/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        groupService.removeMember(
                groupId,
                currentUser.getId(),
                userId
        );
    }


    // Allows a group member to leave the group. The group OWNER cannot leave.
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();

        groupService.leaveGroup(
                groupId,
                currentUser.getId()
        );

        return ResponseEntity.noContent().build();
    }
}
