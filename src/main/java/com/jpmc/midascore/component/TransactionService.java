package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public void process(Transaction transaction) {
        // 1. validate sender exists
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        if (sender == null) return;

        // 2. validate recipient exists
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if (recipient == null) return;

        // 3. validate sender has sufficient balance
        if (sender.getBalance() < transaction.getAmount()) return;

        // 4. record the transaction
        transactionRepository.save(new TransactionRecord(sender, recipient, transaction.getAmount()));

        // 5. update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());
        userRepository.save(sender);
        userRepository.save(recipient);
    }
}