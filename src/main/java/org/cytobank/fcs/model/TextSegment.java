package org.cytobank.fcs.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TextSegment {

  String fullText;

  Map<String, String> kvpair = new HashMap<>();

}
