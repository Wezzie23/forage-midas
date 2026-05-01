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
    private final IncentiveService incentiveService;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository,
                              IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    public void process(Transaction transaction) {
        // validate sender
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        if (sender == null) return;

        // validate recipient
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if (recipient == null) return;

        // validate sufficient balance
        if (sender.getBalance() < transaction.getAmount()) return;

        // get incentive from external API
        float incentiveAmount = incentiveService.getIncentive(transaction);

        // record transaction with incentive
        transactionRepository.save(new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount));

        // update balances — incentive added to recipient only, not deducted from sender
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);
    }
}