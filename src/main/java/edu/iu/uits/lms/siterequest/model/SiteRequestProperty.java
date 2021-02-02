package edu.iu.uits.lms.siterequest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 2/6/15.
 */
@Entity
@Table(name = "SITEREQUEST_PROPERTIES")
@SequenceGenerator(name = "SITEREQUEST_PROPERTIES_ID_SEQ", sequenceName = "SITEREQUEST_PROPERTIES_ID_SEQ", allocationSize = 1)
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class SiteRequestProperty implements Serializable {

    @Id
    @Column(name = "SITEREQUEST_PROPERTIES_ID")
    @GeneratedValue(generator = "SITEREQUEST_PROPERTIES_ID_SEQ")
    private Long id;

    @NonNull
    @Column(name = "prop_key")
    private String key;

    @NonNull
    @Column(name = "prop_value")
    private String value;

    /**
     * The property value contains a comma delimited list.  Return is as a list of strings
     * @return List of parsed values
     */
    public List<String> getPropValueAsList() {
        String[] values = value.split(",");
        return Arrays.asList(values);
    }

    /**
     * The property value contains a comma delimited list.  Return is as a String[]
     * @return List of parsed values
     */
    public String[] getPropValueAsArray() {
        return value.split(",");
    }

}
