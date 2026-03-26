package com.vibes.app.modules.chat.dto;

import java.util.List;
import java.util.UUID;

public class CreateGroupRequest {
    private String name;
    private List<UUID> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<UUID> getMemberIds() { return memberIds; }
    public void setMemberIds(List<UUID> memberIds) { this.memberIds = memberIds; }
}