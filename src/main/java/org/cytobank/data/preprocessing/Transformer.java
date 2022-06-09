package org.cytobank.data.preprocessing;


import org.cytobank.data.model.Table;

public interface Transformer {

  Table transform(Table table);

}
