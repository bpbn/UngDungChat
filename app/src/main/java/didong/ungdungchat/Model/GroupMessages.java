package didong.ungdungchat.Model;

public class GroupMessages {
    private String name;
    private String message;
    private String date;
    private String time;
    private String from;
    private String type;

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    private String messageID;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public GroupMessages() {
    }

    public GroupMessages(String name, String message, String date, String time, String from, String type, String messageID) {
        this.name = name;
        this.message = message;
        this.date = date;
        this.time = time;
        this.from = from;
        this.type = type;
        this.messageID = messageID;
    }
}
