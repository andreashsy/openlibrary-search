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

    // search using the openlibrary api and returns the json string (only contains title and works id)
    public String search(String searchTerm) {    
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
        // throws an exception if response is not OK
        if (resp.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("Error: status code %s".formatted(resp.getStatusCode().toString()));
        }
        return resp.getBody();
    }

    // converts search result json-string into a list of books
    public List<Book> jsonToBookList(String jsonDataString) {     
        // converts string into stream of bytes           
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();
            final JsonArray readings = result.getJsonArray("docs");
            return readings.stream()
                .map(v -> (JsonObject)v)
                .map(w -> {
                    return new Book(w.getString("key").replace("/works/", ""),
                                    w.getString("title"));
                })                    
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new LinkedList<Book>();
        }       
    }

    // returns a list of string, 
    // first element is {1 if data is from cache, 0 if data is from openlibrary api}
    // second element is json string data for a single book
    public List<String> get(String worksId) {
        if (bookCacheSvc.hasKey(worksId)) {
            logger.log(Level.INFO, "Cache hit for works id: " + worksId);
            //if redis cache contains the key, get data from redis cache
            List<String> resultList = new LinkedList<String>();
            resultList.add("1");
            resultList.add(bookCacheSvc.get(worksId));
            return resultList;
        } else {
            //else get data from openlibrary api
            String url = URL_OPENLIBARARY_BASE + "/works/" + worksId + ".json";
            logger.log(Level.INFO, "book url is: " + url);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.getForEntity(url, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new IllegalArgumentException("Error: status code %s".formatted(resp.getStatusCode().toString()));
            }
            String jsonDataString = resp.getBody();
            //cache data from openlibrary api to redis
            bookCacheSvc.cache(worksId, jsonDataString);
            List<String> resultList2 = new LinkedList<String>();
            resultList2.add("0");
            resultList2.add(jsonDataString);
            return resultList2;
        }
        
    }

    // converts json string data into a book object
    public Book jsonToBook(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();

            Book book = new Book();
            logger.log(Level.INFO, "book title is: " + result.getString("title"));
            book.setTitle(result.getString("title"));

            // checks if description exists
            String description = "Not available";
            try {
                if (result.getString("description").trim().length() > 0) {
                    description = result.getString("description");
                }
            } catch (Exception e) {
                logger.log(Level.INFO, "Description not available >>>>" + e.toString());
            }
            book.setDescription(description);

            // checks if excerpt exists
            String excerpt = "Not Available";
            try {
                String excerptFromJson = result.getJsonArray("excerpts").getJsonObject(0).getString("excerpt");
                if (excerptFromJson.trim().length() > 0) {
                    excerpt = excerptFromJson;
                } 
            } catch (Exception e) {
                logger.log(Level.INFO, "Excerpt not available >>>>" + e.toString());
            }   
            book.setExcerpt(excerpt);
            return book;
            
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new Book();
        }
    }
    
}
