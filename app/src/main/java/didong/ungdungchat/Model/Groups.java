package didong.ungdungchat.Model;

public class Groups {
    String name, image, groupID;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Groups() {
    }

    public Groups(String name, String image, String groupID) {
        this.name = name;
        this.image = image;
        this.groupID = groupID;
    }
}
