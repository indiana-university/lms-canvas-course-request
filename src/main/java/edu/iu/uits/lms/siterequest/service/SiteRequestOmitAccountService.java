package edu.iu.uits.lms.siterequest.service;

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteRequestOmitAccountService {
    @Autowired
    private AccountService accountService;

    public static final String DEFAULT_NOTE_FORMAT_STRING = "Account Name (as of this record insertion) is %s";
    public static final int MAXIMUM_NOTE_LENGTH = 255;

    public List<Account> getAccountByName(String name) {
        List<Account> accounts = accountService.getSubAccounts();
        accounts = accounts.stream()
                .filter(account -> name.equals(account.getName()))
                .toList();

        return accounts;
    }

}
