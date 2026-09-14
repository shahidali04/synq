package synq_backend.group.service;

import jakarta.transaction.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import synq_backend.conversation.entity.Conversation;
import synq_backend.conversation.entity.ConversationParticipant;
import synq_backend.conversation.entity.ConversationType;
import synq_backend.conversation.entity.ParticipantRole;
import synq_backend.conversation.repository.ConversationParticipantRepository;
import synq_backend.conversation.repository.ConversationRepository;
import synq_backend.group.dto.CreateGroupRequest;
import synq_backend.group.dto.GroupDTO;
import synq_backend.group.entity.Group;
import synq_backend.group.repository.GroupRepository;
import synq_backend.message.dto.MessageDTO;
import synq_backend.message.entity.Message;
import synq_backend.message.entity.MessageType;
import synq_backend.message.repository.MessageRepository;
import synq_backend.notification.entity.NotificationType;
import synq_backend.notification.service.NotificationService;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;
import synq_backend.group.dto.AddGroupMemberRequest;
import synq_backend.group.dto.GroupMembersDTO;

import java.util.List;

import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GroupService(
            GroupRepository groupRepository,
            ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate
    ){
        this.groupRepository = groupRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public GroupDTO createGroup(
            UUID currentUserId,
            CreateGroupRequest request
    ) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // A group uses the existing Conversation system so messages,
        // WebSocket delivery, pagination, read status, and delivery status
        // can all be reused.
        Conversation conversation = new Conversation(
                ConversationType.GROUP,
                currentUser
        );

        conversation = conversationRepository.save(conversation);

        Group group = new Group(
                conversation,
                request.getName().trim(),
                currentUser
        );

        group = groupRepository.save(group);

        // The creator automatically becomes the group OWNER.
        ConversationParticipant owner =
                new ConversationParticipant(
                        conversation,
                        currentUser,
                        ParticipantRole.OWNER
                );

        participantRepository.save(owner);

        return mapToDTO(group);

    }

    private GroupDTO mapToDTO(Group group) {

        GroupDTO dto = new GroupDTO();

        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setConversationId(group.getConversation().getId());
        dto.setCreatedBy(group.getCreatedBy().getId());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());

        return dto;
    }


    //Only owner can add members , members are not allowed
    @Transactional
    public void addMember(
            UUID groupId,
            UUID currentUserId,
            AddGroupMemberRequest request
    ) {
        // Find the group.
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify that the requester is the group owner.
        ConversationParticipant owner =
                participantRepository
                        .findByConversationIdAndUserId(
                                group.getConversation().getId(),
                                currentUserId
                        )
                        .orElseThrow(() -> new IllegalArgumentException("You are not a member of this group"));

        if (owner.getRole() != ParticipantRole.OWNER) {
            throw new IllegalArgumentException("Only the group owner can add members");
        }

        // Find the user who should be added.
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent adding the same user twice.
        boolean alreadyMember =
                participantRepository
                        .findByConversationIdAndUserId(
                                group.getConversation().getId(),
                                userToAdd.getId()
                        )
                        .isPresent();

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        // Add the user as a MEMBER.
        ConversationParticipant member =
                new ConversationParticipant(
                        group.getConversation(),
                        userToAdd,
                        ParticipantRole.MEMBER
                );

        participantRepository.save(member);

        // Create a SYSTEM message for the group.
        Message systemMessage = new Message(
                group.getConversation(),
                owner.getUser(),
                owner.getUser().getUsername() + " added " +
                        userToAdd.getUsername() +
                        " to the group",
                MessageType.SYSTEM
        );

        Message savedMessage = messageRepository.save(systemMessage);

        MessageDTO systemMessageDTO = new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getDeliveredAt(),
                savedMessage.getReadAt(),
                savedMessage.getDeletedAt() != null,
                savedMessage.getType()
        );

        // Broadcast the SYSTEM event to the group.
        messagingTemplate.convertAndSend(
                "/topic/conversation/" +
                        group.getConversation().getId(),
                systemMessageDTO
        );

        // Notify the newly added member privately.
        notificationService.createNotification(
                userToAdd.getId(),
                NotificationType.GROUP,
                "You have been added to the group " + group.getName(),
                group.getId()
        );
    }


    // Retrieves all members belonging to the specified group.
    public List<GroupMembersDTO> getGroupMembers(UUID groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        List<ConversationParticipant> participants =
                participantRepository.findByConversationId(
                        group.getConversation().getId()
                );

        return participants.stream()
                .map(this::mapToGroupMembersDTO)
                .toList();
    }

    private GroupMembersDTO mapToGroupMembersDTO(
            ConversationParticipant participant
    ) {
        GroupMembersDTO dto = new GroupMembersDTO();

        dto.setUserId(participant.getUser().getId());
        dto.setUsername(participant.getUser().getUsername());
        dto.setDisplayName(participant.getUser().getDisplayName());
        dto.setRole(participant.getRole());

        return dto;
    }


    // Removes a member from the group. Only the group OWNER can perform this action.
    @Transactional
    public void removeMember(
            UUID groupId,
            UUID currentUserId,
            UUID userIdToRemove
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Verify that the requester is the group owner.
        ConversationParticipant owner =
                participantRepository
                        .findByConversationIdAndUserId(
                                group.getConversation().getId(),
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "You are not a member of this group"
                                )
                        );

        if (owner.getRole() != ParticipantRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the group owner can remove members"
            );
        }

        // Prevent the owner from removing themselves.
        if (currentUserId.equals(userIdToRemove)) {
            throw new IllegalArgumentException(
                    "The group owner cannot remove themselves"
            );
        }

        // Find the member who should be removed.
        ConversationParticipant member =
                participantRepository
                        .findByConversationIdAndUserId(
                                group.getConversation().getId(),
                                userIdToRemove
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User is not a member of this group"
                                )
                        );

        // Remove the user's membership without deleting their messages.
        participantRepository.delete(member);

        // Create a SYSTEM message for the remaining group members.
        Message systemMessage = new Message(
                group.getConversation(),
                owner.getUser(),
                owner.getUser().getUsername() + " removed " +
                        member.getUser().getUsername() +
                        " from the group",
                MessageType.SYSTEM
        );

        Message savedMessage = messageRepository.save(systemMessage);

        MessageDTO systemMessageDTO = new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getDeliveredAt(),
                savedMessage.getReadAt(),
                savedMessage.getDeletedAt() != null,
                savedMessage.getType()
        );

        // Broadcast the SYSTEM event to the group.
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + group.getConversation().getId(),
                systemMessageDTO
        );

        // Send a private notification to the removed user.
        notificationService.createNotification(
                userIdToRemove,
                NotificationType.GROUP,
                "You were removed from the group " + group.getName(),
                group.getId()
        );
    }


    //Only members can leave the group, owner can't
    @Transactional
    public void leaveGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Group not found"));

        // Find the current user's membership in the group.
        ConversationParticipant member =
                participantRepository
                        .findByConversationIdAndUserId(
                                group.getConversation().getId(),
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "You are not a member of this group"
                                )
                        );

        // Prevent the group owner from leaving.
        if (member.getRole() == ParticipantRole.OWNER) {
            throw new IllegalArgumentException(
                    "The group owner cannot leave the group"
            );
        }

        // Remove the user's membership without deleting their messages.
        participantRepository.delete(member);

        // Create a SYSTEM message for the remaining group members.
        Message systemMessage = new Message(
                group.getConversation(),
                member.getUser(),
                member.getUser().getUsername() + " left the group",
                MessageType.SYSTEM
        );

        Message savedMessage = messageRepository.save(systemMessage);

        MessageDTO systemMessageDTO = new MessageDTO(
                savedMessage.getId(),
                savedMessage.getConversation().getId(),
                savedMessage.getSender().getId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getDeliveredAt(),
                savedMessage.getReadAt(),
                savedMessage.getDeletedAt() != null,
                savedMessage.getType()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation/" + group.getConversation().getId(),
                systemMessageDTO
        );
    }

}
