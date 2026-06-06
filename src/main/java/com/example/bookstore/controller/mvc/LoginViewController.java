package com.example.bookstore.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller rendering the custom login page.
 */
@Controller
public class LoginViewController {

    /**
     * Renders the custom login page.
     *
     * @return the name of the login Thymeleaf template (login.html).
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}
