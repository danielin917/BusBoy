package solarcar.vdcListener;


public final class SolarChatMessage {

    private String id;
    private String text;
    private String message;
    private String type;

    /*
     * public ConcurrentHashMap<String, Double> getMap() { return map;
	}
     */
    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public SolarChatMessage(String message) {
        this.message = message;
        this.id = "NULL";
        this.type = message.substring(0, 4);

        message = message.substring(5);
        int msg_start = message.indexOf("msg='") + "msg='".length();
        int msg_end = message.indexOf("'", msg_start + 1);
        text = message.substring(msg_start, msg_end);
        id = message.substring(message.indexOf("id='") + "id='".length(),
                message.indexOf("'", message.indexOf("id='") + "id='".length() + 1));

    }

    public SolarChatMessage(String id, String text) {
        this.type = "chat";
        this.id = id;
        this.text = text;
        this.message = "chat id='" + id + "' msg='" + text + "'";
    }
}