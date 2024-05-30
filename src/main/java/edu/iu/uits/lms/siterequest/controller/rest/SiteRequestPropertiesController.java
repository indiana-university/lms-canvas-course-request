package edu.iu.uits.lms.siterequest.controller.rest;

/*-
 * #%L
 * siterequest
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import edu.iu.uits.lms.siterequest.model.SiteRequestProperty;
import edu.iu.uits.lms.siterequest.repository.SiteRequestPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/props")
public class SiteRequestPropertiesController {

   @Autowired
   private SiteRequestPropertyRepository siteRequestPropertyRepository = null;

   @GetMapping("/key")
   public SiteRequestProperty getByKey(@RequestParam("key") String key) {
      return siteRequestPropertyRepository.findByKey(key);
   }

   @GetMapping("/all")
   public List<SiteRequestProperty> getAll() {
      return (List<SiteRequestProperty>) siteRequestPropertyRepository.findAll();
   }

   @PutMapping("/{id}")
   public SiteRequestProperty update(@PathVariable Long id, @RequestBody SiteRequestProperty siteRequestProperty) {
      SiteRequestProperty updated = siteRequestPropertyRepository.findById(id).orElse(null);

      if (siteRequestProperty.getKey() != null) {
         updated.setKey(siteRequestProperty.getKey());
      }
      if (siteRequestProperty.getValue() != null) {
         updated.setValue(siteRequestProperty.getValue());
      }

      return siteRequestPropertyRepository.save(updated);
   }

   @PostMapping("/")
   public SiteRequestProperty create(@RequestBody SiteRequestProperty siteRequestProperty) {
      return siteRequestPropertyRepository.save(siteRequestProperty);
   }

   @DeleteMapping("/{id}")
   public String delete(@PathVariable Long id) {
      siteRequestPropertyRepository.deleteById(id);
      return "Delete success.";
   }
}
