package ibf2021.assessment.openlibrarysearch.model;

public class Book {
    private String title;
    private String key;

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
    
}
