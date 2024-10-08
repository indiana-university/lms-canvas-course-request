package edu.iu.uits.lms.siterequest.model;

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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
