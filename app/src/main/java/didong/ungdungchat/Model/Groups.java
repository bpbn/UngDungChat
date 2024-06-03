package didong.ungdungchat.Model;

public class Groups {
    String name, image;

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

    public Groups(String name, String image) {
        this.name = name;
        this.image = image;
    }
}
