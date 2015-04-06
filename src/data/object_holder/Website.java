package data.object_holder;

@SuppressWarnings("UnusedDeclaration")
public class Website {
    String url;
    String name;
    int imageUri;

    public int getImageUri() {
        return imageUri;
    }

    public Website setImageUri(int imageUri) {
        this.imageUri = imageUri;
        return this;
    }

    public String getName() {
        return name;
    }

    public Website setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Website setUrl(String url) {
        this.url = url;
        return this;
    }

}
