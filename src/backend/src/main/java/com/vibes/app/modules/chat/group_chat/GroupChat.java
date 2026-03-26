package com.vibes.app.modules.chat.group_chat;

import jakarta.persistence.*;
import com.vibes.app.modules.chat.Chat;
import com.vibes.app.modules.auth.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_chats")
public class GroupChat implements Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String groupPictureUrl;

    /**
     * The user who created the group — has admin privileges.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * All members of the group, including the creator.
     * Join table: group_chat_members(group_chat_id, user_id)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_chat_members",
        joinColumns = @JoinColumn(name = "group_chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupChat() {}

    public GroupChat(String name, User creator, List<User> members) {
        this.name = name;
        this.creator = creator;
        this.members = new ArrayList<>(members);
        // Ensure creator is always in the members list
        if (!this.members.contains(creator)) {
            this.members.add(creator);
        }
    }

    @Override
    public String getChatId() {
        return id != null ? id.toString() : null;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGroupPictureUrl() { return groupPictureUrl; }
    public void setGroupPictureUrl(String groupPictureUrl) { this.groupPictureUrl = groupPictureUrl; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }
    public void addMember(User user) { if (!members.contains(user)) members.add(user); }
    public void removeMember(User user) { members.remove(user); }
    public LocalDateTime getCreatedAt() { return createdAt; }
}