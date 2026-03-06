import message.Message;

public class PrivateChat implements Chat {
    
    @Override
    public void sendMessage(Message message) {
        System.out.println("Private message from " + message.getSenderId() + ": " + message.getContent());
    }

    @Override 
    public Message recieveMessage() {
        return new Message("user2", "Greetings!");
    }

    @Override 
    public Message editMessage(Message message) {
        return message;
    }
}