package com.example.demo.Controller;


import com.example.demo.Entity.Dto.AccountDTO;
import com.example.demo.Service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/fetchAccountData")
    public ResponseEntity<?> getAccountDetails(@RequestParam String userId) {
        try {
            AccountDTO res = accountService.fetchAccountData(userId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong!");

        }
    }
}
