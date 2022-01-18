package ibf2021.assessment.openlibrarysearch.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;

@Controller
@RequestMapping(path="/search", produces = MediaType.TEXT_HTML_VALUE)
public class SearchController {

    @GetMapping
    public String getSearchResult(@RequestParam(required=true) String searchTitle, Model model) {
        model.addAttribute("searchTitle", searchTitle);
        return "results";
    }

    @GetMapping(path = "/main")
    public String backToSearch() {
        return "index";
    }
    
}
