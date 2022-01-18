package ibf2021.assessment.openlibrarysearch.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ibf2021.assessment.openlibrarysearch.model.Book;
import ibf2021.assessment.openlibrarysearch.services.BookService;

@Controller
@RequestMapping(path="/book", produces=MediaType.TEXT_HTML_VALUE)
public class BookController {
    private final Logger logger = Logger.getLogger(BookController.class.getName());
    @Autowired
    BookService bookSvc;

    @GetMapping("/{worksId}")
    public String showBookInfo(@PathVariable String worksId, Model model) {
        List<String> bookJson = bookSvc.get(worksId);
        logger.log(Level.INFO, "book json data is: " + bookJson);
        Book bookObj = bookSvc.jsonToBook(bookJson.get(1));
        if (bookJson.get(0) == "1") {
            bookObj.setCached(true);
        } else if (bookJson.get(0) == "0") {
            bookObj.setCached(false);
        }
        model.addAttribute("bookObj", bookObj);
        return "bookinfo";
    }
    
}
