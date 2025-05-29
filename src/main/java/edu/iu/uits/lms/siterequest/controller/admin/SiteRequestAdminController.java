package edu.iu.uits.lms.siterequest.controller.admin;

/*-
 * #%L
 * siterequest
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import edu.iu.uits.lms.siterequest.model.SiteRequestHiddenAccount;
import edu.iu.uits.lms.siterequest.repository.SiteRequestHiddenAccountRepository;
import edu.iu.uits.lms.siterequest.service.Constants;
import edu.iu.uits.lms.siterequest.service.SiteRequestHiddenAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/app/admin/hideaccount")
@Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
@Slf4j
public class SiteRequestAdminController extends OidcTokenAwareController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private SiteRequestHiddenAccountRepository siteRequestHiddenAccountRepository;

    @RequestMapping("/launch")
    public String adminMain(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        List<SiteRequestHiddenAccount> siteRequestHiddenAccounts = siteRequestHiddenAccountRepository.findAll();

        model.addAttribute("hiddenAccounts", siteRequestHiddenAccounts);

        return "admin/hiddenAccount";
    }

    @RequestMapping("/{hiddenAccountId}/edit")
    public String adminOmitAccountEdit(@PathVariable("hiddenAccountId") String hiddenAccountId, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        SiteRequestHiddenAccount siteRequestHiddenAccount = siteRequestHiddenAccountRepository.findById(Long.parseLong(hiddenAccountId)).orElse(null);

        if (siteRequestHiddenAccount != null) {
            model.addAttribute("hiddenAccountForm", siteRequestHiddenAccount);
        }

        return "admin/editHiddenAccount";
    }

    @RequestMapping("/update")
    public String adminOmitAccountUpdate(@RequestParam("action") String action, SiteRequestHiddenAccount siteRequestHiddenAccount, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        if ("submit".equalsIgnoreCase(action)) {
            if (siteRequestHiddenAccount.getNote() != null) {
                final int noteLength = siteRequestHiddenAccount.getNote().length();

                // if the note is longer than the database field, truncate the note to the database field length
                siteRequestHiddenAccount.setNote(siteRequestHiddenAccount.getNote().substring(0,
                        Math.min(noteLength, SiteRequestHiddenAccountService.MAXIMUM_NOTE_LENGTH)));
            }

            siteRequestHiddenAccountRepository.save(siteRequestHiddenAccount);
        }

        return adminMain(model);
    }

    @RequestMapping("/new")
    public String adminOmitAccountNew(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        model.addAttribute("create", true);

        SiteRequestHiddenAccount siteRequestHiddenAccount = new SiteRequestHiddenAccount();
        siteRequestHiddenAccount.setUserAddedBy(oidcTokenUtils.getUserLoginId());
        model.addAttribute("hiddenAccountForm", siteRequestHiddenAccount);

        return "admin/editHiddenAccount";
     }

    @RequestMapping("/submit")
    public String adminOmitAccountCreate(@RequestParam("action") String action, SiteRequestHiddenAccount siteRequestHiddenAccount, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        if ("submit".equalsIgnoreCase(action)) {
            Optional<SiteRequestHiddenAccount> existingSiteRequestHiddenAccount = siteRequestHiddenAccountRepository.findById(siteRequestHiddenAccount.getAccountIdToHide());

            if (existingSiteRequestHiddenAccount.isPresent()) {
                model.addAttribute("create", true);
                model.addAttribute("backendValidationError",
                        String.format("Account ID '%s' is already hidden", siteRequestHiddenAccount.getAccountIdToHide()));
                model.addAttribute("hiddenAccountForm", siteRequestHiddenAccount);
                return "admin/editHiddenAccount";
            }

            if (siteRequestHiddenAccount.getNote() != null) {
                final int noteLength = siteRequestHiddenAccount.getNote().length();

                // if the note is longer than the database field, truncate the note to the database field length
                siteRequestHiddenAccount.setNote(siteRequestHiddenAccount.getNote().substring(0,
                        Math.min(noteLength, SiteRequestHiddenAccountService.MAXIMUM_NOTE_LENGTH)));
            }

            final Account account = accountService.getAccount(siteRequestHiddenAccount.getAccountIdToHide().toString());

            if (account == null) {
                model.addAttribute("create", true);
                model.addAttribute("backendValidationError",
                        String.format("Invalid Account ID '%s'", siteRequestHiddenAccount.getAccountIdToHide()));
                model.addAttribute("hiddenAccountForm", siteRequestHiddenAccount);
                return "admin/editHiddenAccount";
            }

            siteRequestHiddenAccount.setAccountNameToHide(account.getName());
            siteRequestHiddenAccountRepository.save(siteRequestHiddenAccount);
        }

        return adminMain(model);
    }

    @RequestMapping("/{hiddenAccountId}/delete")
    public String adminOmitAccountDelete(@PathVariable("hiddenAccountId") String hiddenAccountId, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        siteRequestHiddenAccountRepository.deleteById(Long.parseLong(hiddenAccountId));

        return adminMain(model);
    }
}