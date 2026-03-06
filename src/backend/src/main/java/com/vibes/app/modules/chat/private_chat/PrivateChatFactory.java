public class PrivateChatFactory implements ChatFactory {

    @Override
    public Chat createChat() {
        return new PrivateChat();
    }

    @Override 
    public ChatSettings createSettings() {
        return new PrivateChatSettings();
    }

}