package com.ledgerflow.core.config;

import com.ledgerflow.core.model.Account;
import com.ledgerflow.core.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (accountRepository.count() == 0) {
            Account alice = new Account();
            alice.setAccountNumber("ACC_ALICE");
            alice.setCurrency("USD");
            alice.setBalance(new BigDecimal("1000.00")); // Alice starts with $1000

            Account bob = new Account();
            bob.setAccountNumber("ACC_BOB");
            bob.setCurrency("USD");
            bob.setBalance(new BigDecimal("0.00")); // Bob starts with $0

            accountRepository.saveAll(Arrays.asList(alice, bob));
            System.out.println("✅ Test Data Seeded: Alice ($1000), Bob ($0)");
        }
    }
}