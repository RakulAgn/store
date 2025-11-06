package com.buggybot.store.controller.store;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class StoreController {

    @GetMapping("/")
    public String getStoreInformation() {
        return "This is Store Information";
    }
}
