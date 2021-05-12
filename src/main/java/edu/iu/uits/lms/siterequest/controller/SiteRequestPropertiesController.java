package edu.iu.uits.lms.siterequest.controller;

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
