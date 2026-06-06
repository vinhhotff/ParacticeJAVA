package com.example.bookstore.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller handling root redirects.
 */
@Controller
public class HomeController {

    /**
     * Redirects the root path (/) to the books path (/books).
     *
     * @return redirect prefix with books path.
     */
    @GetMapping("/")
    public String redirectToBooks() {
        return "redirect:/books";
    }
}
