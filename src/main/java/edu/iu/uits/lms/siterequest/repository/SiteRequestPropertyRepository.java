package edu.iu.uits.lms.siterequest.repository;

import edu.iu.uits.lms.siterequest.model.SiteRequestProperty;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface SiteRequestPropertyRepository extends PagingAndSortingRepository<SiteRequestProperty, Long> {

    SiteRequestProperty findByKey(String key);
}
