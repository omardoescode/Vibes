public enum WebSocketManager {
    INSTANCE;

    private Map<String, Object> sockets = new ConcurrentHashMap<>();

    public void addWebSocket(String userId, Object socket) {
        sockets.put(userId, socket);
    }

    public Object getWebSocket(String userId) {
        return sockets.get(userId);
    }

    public void removeWebSocket(String userId) {
        sockets.remove(userId);
    }
}