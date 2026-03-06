import message.Message;

public interface Chat {
    
    void sendMessage(Message message);
    
    String receiveMessage();
    
    String editMessage(Message message);

}