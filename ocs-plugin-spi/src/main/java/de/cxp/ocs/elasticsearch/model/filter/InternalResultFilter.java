package de.cxp.ocs.elasticsearch.model.filter;

import de.cxp.ocs.config.Field;

public interface InternalResultFilter {

	Field getField();

	boolean isFilterOnId();

	boolean isNegated();

	String getFieldPrefix();

	boolean isNestedFilter();

	String[] getValues();

}