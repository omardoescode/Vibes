package com.vibes.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import com.vibes.app.modules.chat.private_chat.PrivateChat;
import com.vibes.app.modules.chat.private_chat.PrivateChatSettings;

@Component
public class TestHibernate implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create a new chat
        PrivateChat chat = new PrivateChat();
        chat.setUser1Id(1L);
        chat.setUser2Id(2L);
        em.persist(chat);

        // Create chat settings
        PrivateChatSettings settings = new PrivateChatSettings();
        settings.setChatId(chat.getId());
        settings.setNotificationsEnabled(true);
        em.persist(settings);

        em.flush();

        System.out.println("Chat ID: " + chat.getId());
        System.out.println("Settings ID: " + settings.getId());

        // Fetch back from DB
        PrivateChat chatFromDb = em.find(PrivateChat.class, chat.getId());
        PrivateChatSettings settingsFromDb = em.find(PrivateChatSettings.class, settings.getId());

        System.out.println("Chat from DB user1Id: " + chatFromDb.getUser1Id());
        System.out.println("Settings from DB notificationsEnabled: " + settingsFromDb.isNotificationsEnabled());
    }
}