package com.aws.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class BotController {

    @Autowired
    LexService lex;

    @GetMapping("/index")
    public String greetingForm(Model model) {
        return "index";
    }

    // Handles a string posted from the client.
    @RequestMapping(value = "/text", method = RequestMethod.POST)
    @ResponseBody
    String addItems(HttpServletRequest request, HttpServletResponse response) {

        String text = request.getParameter("text");
        String message = lex.getText(text);
        return message;
    }
}