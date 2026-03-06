public class PrivateChatSettings implements ChatSettings  {
    
    @Override 
    public void changeSettings() {
        System.out.println("Changing private chat settings");
    }

    @Override 
    public void enableNotifications() {
        System.out.println("Notifications enabled");
    }

    @Override 
    public void disableNotifications() {
        System.out.println("Notifications disabled");
    }
    
}