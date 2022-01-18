package ibf2021.assessment.openlibrarysearch.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import static ibf2021.assessment.openlibrarysearch.Constants.*;

@Service
public class BookService {
    private static final Logger logger = Logger.getLogger(BookService.class.getName());

    public String search(String searchTerm) {
        String encodedSearchTerm = searchTerm.replace(" ", "+");
        // https://openlibrary.org/search.json?q=harry%20potter&fields=docs,title,key&limit=20
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

    public static List<Book> jsonToBook(String jsonDataString) {
        try (InputStream is = new ByteArrayInputStream(jsonDataString.getBytes())) {
            final JsonReader reader = Json.createReader(is);
            final JsonObject result = reader.readObject();
            final JsonArray readings = result.getJsonArray("docs");

            LinkedList<Book> bookList = new LinkedList<Book>();
            
            return bookList;
            // return readings.stream()
            //     .map(v -> (JsonObject)v)
            //     .map(Weather::create)
            //     .map(w -> {
            //         w.setCity(cityName);
            //         w.setTemp(temp);
            //         return w;
            //     })                    
            //     .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.INFO, e.toString());
            return new LinkedList<Book>();
        }
        
    }
    
}
