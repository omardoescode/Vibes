package com.vibes.app.modules.chat.controllers;

import com.vibes.app.modules.auth.repositories.UserCredentialsRepository;
import com.vibes.app.modules.chat.dto.CreateGroupRequest;
import com.vibes.app.modules.chat.dto.GroupChatResponse;
import com.vibes.app.modules.chat.services.GroupChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final UserCredentialsRepository userCredentialsRepository;

    public GroupChatController(GroupChatService groupChatService,
                               UserCredentialsRepository userCredentialsRepository) {
        this.groupChatService = groupChatService;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    /**
     * Create a new group chat.
     * POST /groups
     * Body: { "name": "...", "memberIds": ["uuid1", "uuid2"] }
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request,
                                         Authentication authentication) {
        try {
            UUID creatorId = resolveUserId(authentication);
            GroupChatResponse response = groupChatService.createGroup(creatorId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * List all group chats the authenticated user belongs to.
     * GET /groups
     */
    @GetMapping
    public ResponseEntity<List<GroupChatResponse>> listGroups(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        return ResponseEntity.ok(groupChatService.getGroupsForUser(userId));
    }

    /**
     * Add a member to a group. Only the group admin can do this.
     * POST /groups/{groupId}/members?userId={uuid}
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMember(@PathVariable UUID groupId,
                                       @RequestParam UUID userId,
                                       Authentication authentication) {
        try {
            UUID requesterId = resolveUserId(authentication);
            GroupChatResponse response = groupChatService.addMember(groupId, requesterId, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove a member from a group. Only the group admin can do this.
     * DELETE /groups/{groupId}/members/{userId}
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable UUID groupId,
                                          @PathVariable UUID userId,
                                          Authentication authentication) {
        try {
            UUID requesterId = resolveUserId(authentication);
            GroupChatResponse response = groupChatService.removeMember(groupId, requesterId, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private UUID resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        return userCredentialsRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getUser().getId();
    }
}