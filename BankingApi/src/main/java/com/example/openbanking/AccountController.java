package com.example.openbanking;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AccountController {

    @GetMapping("/api/accounts")
    public List<Map<String, String>> getAccounts() {

        return List.of(
                Map.of(
                        "accountId", "ACC1001",
                        "accountType", "CURRENT",
                        "currency", "GBP",
                        "status", "ENABLED"
                ),
                Map.of(
                        "accountId", "ACC1002",
                        "accountType", "SAVINGS",
                        "currency", "GBP",
                        "status", "ENABLED"
                )
        );
    }
}