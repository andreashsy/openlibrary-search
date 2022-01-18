package ibf2021.assessment.openlibrarysearch.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ibf2021.assessment.openlibrarysearch.model.Book;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import static ibf2021.assessment.openlibrarysearch.Constants.*;

@Service
public class BookService {
    private final Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    BookCacheService bookCacheSvc;

    public String search(String searchTerm) {    // search using the openlibrary api and returns the json string
        String encodedSearchTerm = searchTerm.replace(" ", "+");
        String url = UriComponentsBuilder
            .fromUriString(URL_OPENLIBARARY_BASE + "/search.json")
            .queryParam("title", encodedSearchTerm)
            .queryParam("fields", "title,key")
            .queryParam("limit", "20")
            .toUriString();
        logger.log(Level.INFO, "search url is: " + url);
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(url, String.class);
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("Error: status code %s".formatted(resp.getStatusCode().toString()));
        }
        String jsonDataString = resp.getBody();

        return jsonDataString;
    }

    public List<Book> jsonToBookList(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();
            final JsonArray readings = result.getJsonArray("docs");

            LinkedList<Book> bookList = new LinkedList<Book>();
            for (JsonValue jv:readings) {
                JsonObject jo = jv.asJsonObject();
                String key = jo.getString("key").replace("/works/", "");
                String title = jo.getString("title");
                // logger.log(Level.INFO, "key: %s, title: %s".formatted(key, title));
                Book book = new Book(key, title);
                bookList.add(book);
            }
            return bookList;

        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new LinkedList<Book>();
        }
        
    }

    public List<String> get(String worksId) {
        if (bookCacheSvc.hasKey(worksId)) {
            //get from redis cache
            logger.log(Level.INFO, "Cache hit for works id: " + worksId);
            List<String> resultList = new LinkedList<String>();
            resultList.add("1");
            resultList.add(bookCacheSvc.get(worksId));
            return resultList;
        } else {
            //get from openlibrary api
            String url = URL_OPENLIBARARY_BASE + "/works/" + worksId + ".json";
            logger.log(Level.INFO, "book url is: " + url);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.getForEntity(url, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new IllegalArgumentException("Error: status code %s".formatted(resp.getStatusCode().toString()));
            }
            String jsonDataString = resp.getBody();
            //cache data from openlibrary api
            bookCacheSvc.cache(worksId, jsonDataString);
            List<String> resultList2 = new LinkedList<String>();
            resultList2.add("0");
            resultList2.add(jsonDataString);
            return resultList2;
        }
        
    }

    public Book jsonToBook(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();

            Book book = new Book();
            logger.log(Level.INFO, "book title is: " + result.getString("title"));
            book.setTitle(result.getString("title"));

            String description = "Not available";
            try {
                if (result.getString("description").trim().length() > 0) {
                    description = result.getString("description");
                    logger.log(Level.INFO, "description found: " + description);
                }
            } catch (Exception e) {
                logger.log(Level.INFO, "Description not available");
                logger.log(Level.INFO, e.toString());
            }
            book.setDescription(description);

            String excerpt = "Not Available";
            try {
                String excerptFromJson = result.getJsonArray("excerpts").getJsonObject(0).getString("excerpt");
                if (excerptFromJson.trim().length() > 0) {
                    excerpt = excerptFromJson;
                    logger.log(Level.INFO, "excerpt found: " + excerpt);
                } 
            } catch (Exception e) {
                logger.log(Level.INFO, "Excerpt not available");
                logger.log(Level.INFO, e.toString());
            }   
            book.setExcerpt(excerpt);

            return book;
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new Book();
        }
    }
    
}
