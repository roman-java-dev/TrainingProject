package org.example.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Item {
    @JacksonXmlProperty(localName = "value")
    private String value;
    @JacksonXmlProperty(localName = "count")
    private Integer count;
}
