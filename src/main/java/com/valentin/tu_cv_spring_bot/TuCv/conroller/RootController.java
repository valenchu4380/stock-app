package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String root() {
        return "forward:/productos";
    }
}
