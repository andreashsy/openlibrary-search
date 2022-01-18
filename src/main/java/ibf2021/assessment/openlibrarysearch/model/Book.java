package ibf2021.assessment.openlibrarysearch.model;

public class Book {
    private String title;
    private String key;
    private String excerpt = "Not Available";
    private String description = "Not Available";
    private String cached;

    public Book() {

    }

    public Book(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getExcerpt() {
        return this.excerpt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setCached(String cached) {
        this.cached = cached;
    }

    public String getCached() {
        return this.cached;
    }
    
}
