package ibf2021.assessment.openlibrarysearch.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ibf2021.assessment.openlibrarysearch.services.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.logging.Level;
import java.util.logging.Logger;
@Controller
@RequestMapping(path="/search", produces = MediaType.TEXT_HTML_VALUE)
public class SearchController {
    private final Logger logger = Logger.getLogger(SearchController.class.getName());

    @Autowired
    BookService bookSvc;

    @GetMapping
    public String getSearchResult(@RequestParam(required=true) String searchTitle, Model model) {
        model.addAttribute("searchTitle", searchTitle);
        logger.log(Level.INFO, "search result in json: " + bookSvc.search(searchTitle));
        return "results";
    }

    @GetMapping(path = "/main")
    public String backToSearch() {
        return "index";
    }
    
}
