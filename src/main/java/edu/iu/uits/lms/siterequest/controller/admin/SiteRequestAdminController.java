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
import edu.iu.uits.lms.siterequest.model.SiteRequestAccountOmit;
import edu.iu.uits.lms.siterequest.repository.SiteRequestAccountOmitRepository;
import edu.iu.uits.lms.siterequest.service.Constants;
import edu.iu.uits.lms.siterequest.service.SiteRequestOmitAccountService;
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

@Controller
@RequestMapping("/app/admin/omitaccount")
@Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
@Slf4j
public class SiteRequestAdminController extends OidcTokenAwareController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private SiteRequestAccountOmitRepository siteRequestAccountOmitRepository;

    @RequestMapping("/launch")
    public String adminMain(Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        List<SiteRequestAccountOmit> siteRequestAccountOmits = siteRequestAccountOmitRepository.findAll();

        model.addAttribute("omitAccounts", siteRequestAccountOmits);

        return "admin/omitAccount";
    }

    @RequestMapping("/{omitAccountId}/edit")
    public String adminOmitAccountEdit(@PathVariable("omitAccountId") String omitAccountId, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        SiteRequestAccountOmit siteRequestAccountOmit = siteRequestAccountOmitRepository.findById(Long.parseLong(omitAccountId)).orElse(null);

        if (siteRequestAccountOmit != null) {
            model.addAttribute("omitAccountForm", siteRequestAccountOmit);
        }

        return "admin/editOmitAccount";
    }

    @RequestMapping("/update")
    public String adminOmitAccountUpdate(@RequestParam("action") String action, SiteRequestAccountOmit siteRequestAccountOmit, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        if ("submit".equalsIgnoreCase(action)) {
            if (siteRequestAccountOmit.getNote() != null) {
                final int noteLength = siteRequestAccountOmit.getNote().length();

                // if the note is longer than the database field, truncate the note to the database field length
                siteRequestAccountOmit.setNote(siteRequestAccountOmit.getNote().substring(0,
                        Math.min(noteLength, SiteRequestOmitAccountService.MAXIMUM_NOTE_LENGTH)));
            }

            siteRequestAccountOmitRepository.save(siteRequestAccountOmit);
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

        SiteRequestAccountOmit siteRequestAccountOmit = new SiteRequestAccountOmit();
        siteRequestAccountOmit.setUserAddedBy(oidcTokenUtils.getUserLoginId());
        model.addAttribute("omitAccountForm", siteRequestAccountOmit);

        return "admin/editOmitAccount";
     }

    @RequestMapping("/submit")
    public String adminOmitAccountCreate(@RequestParam("action") String action, SiteRequestAccountOmit siteRequestAccountOmit, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        if ("submit".equalsIgnoreCase(action)) {
            if (siteRequestAccountOmit.getNote() != null) {
                final int noteLength = siteRequestAccountOmit.getNote().length();

                // if the note is longer than the database field, truncate the note to the database field length
                siteRequestAccountOmit.setNote(siteRequestAccountOmit.getNote().substring(0,
                        Math.min(noteLength, SiteRequestOmitAccountService.MAXIMUM_NOTE_LENGTH)));
            }

            if (siteRequestAccountOmit.getNote() == null || siteRequestAccountOmit.getNote().isEmpty()) {
                final Account account = accountService.getAccount(siteRequestAccountOmit.getAccountIdToOmit().toString());

                if (account != null) {
                    siteRequestAccountOmit.setNote(String.format(SiteRequestOmitAccountService.DEFAULT_NOTE_FORMAT_STRING, account.getName()));
                }
            }

            siteRequestAccountOmitRepository.save(siteRequestAccountOmit);
        }

        return adminMain(model);
    }

    @RequestMapping("/{omitAccountId}/delete")
    public String adminOmitAccountDelete(@PathVariable("omitAccountId") String omitAccountId, Model model) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);

        if (Boolean.parseBoolean(oidcTokenUtils.getCustomValue(Constants.IS_FRONTEND_MODE))) {
            return "siterequest_error";
        }

        siteRequestAccountOmitRepository.deleteById(Long.parseLong(omitAccountId));

        return adminMain(model);
    }
}