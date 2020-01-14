package de.cxp.ocs.elasticsearch.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import de.cxp.ocs.config.FacetConfiguration;
import de.cxp.ocs.config.FacetConfiguration.FacetConfig;
import de.cxp.ocs.config.FieldConstants;
import de.cxp.ocs.config.FieldType;
import de.cxp.ocs.elasticsearch.query.filter.InternalResultFilter;
import de.cxp.ocs.elasticsearch.query.filter.TermResultFilter;
import de.cxp.ocs.model.result.Facet;
import de.cxp.ocs.util.InternalSearchParams;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class TermFacetCreator implements NestedFacetCreator {

	private static String	GENERAL_TERM_FACET_AGG	= "_term_facet";
	private static String	FACET_NAMES_AGG			= "_names";
	private static String	FACET_VALUES_AGG		= "_values";

	private final Map<String, FacetConfig> facetsBySourceField = new HashMap<>();

	public TermFacetCreator(FacetConfiguration facetConf) {
		facetConf.getFacets().forEach(fc -> facetsBySourceField.put(fc.getSourceField(), fc));
	}

	@Setter
	private int maxFacets = 5;

	@Setter
	private int maxFacetValues = 100;

	@Setter
	private NestedFacetCountCorrector nestedFacetCorrector = null;

	@Override
	public AbstractAggregationBuilder<?> buildAggregation(InternalSearchParams parameters) {
		// TODO: for multi-select facets, filter facets accordingly

		String nestedPathPrefix = "";
		if (nestedFacetCorrector != null) nestedPathPrefix = nestedFacetCorrector.getNestedPathPrefix();

		TermsAggregationBuilder valueAggBuilder = AggregationBuilders.terms(FACET_VALUES_AGG)
				.field(nestedPathPrefix + FieldConstants.TERM_FACET_DATA + ".value")
				.size(maxFacetValues);
		if (nestedFacetCorrector != null) nestedFacetCorrector.correctValueAggBuilder(valueAggBuilder);

		return AggregationBuilders.nested(GENERAL_TERM_FACET_AGG, nestedPathPrefix + FieldConstants.TERM_FACET_DATA)
				.subAggregation(
						AggregationBuilders.terms(FACET_NAMES_AGG)
								.field(nestedPathPrefix + FieldConstants.TERM_FACET_DATA + ".name")
								.size(maxFacets)
								.subAggregation(valueAggBuilder));
	}

	@Override
	public Collection<Facet> createFacets(List<InternalResultFilter> filters, Aggregations aggResult) {
		Terms facetNames = ((Nested) aggResult.get(GENERAL_TERM_FACET_AGG))
				.getAggregations().get(FACET_NAMES_AGG);

		// TODO: optimize SearchParams object to avoid such index creation!
		Map<String, InternalResultFilter> filtersByName = new HashMap<>();
		filters.forEach(p -> filtersByName.put(p.getField(), p));

		List<Facet> termFacets = new ArrayList<>();
		for (Bucket facetNameBucket : facetNames.getBuckets()) {
			Facet facet = new Facet(facetNameBucket.getKeyAsString());
			facet.setType(FieldType.string.name());

			// TODO: this code chunk could be abstracted together with
			// NumberFacetCreator
			InternalResultFilter facetFilter = filtersByName.get(facetNameBucket.getKeyAsString());
			if (facetFilter != null && facetFilter instanceof TermResultFilter) {
				FacetConfig facetConfig = facetsBySourceField.get(facetNameBucket.getKeyAsString());
				if (facetConfig == null || !facetConfig.isMultiSelect()) {
					fillSingleSelectFacet(facetNameBucket, facet, (TermResultFilter) facetFilter);
				}
				else {
					// multiselect facet
					fillFacet(facet, facetNameBucket);
				}
			}
			else {
				// unfiltered facet
				fillFacet(facet, facetNameBucket);
			}

			termFacets.add(facet);
		}

		return termFacets;
	}

	private void fillSingleSelectFacet(Bucket facetNameBucket, Facet facet, TermResultFilter facetFilter) {
		Terms facetValues = ((Terms) facetNameBucket.getAggregations().get(FACET_VALUES_AGG));
		long absDocCount = 0;
		for (String filterValue : facetFilter.getValues()) {
			Bucket elementBucket = facetValues.getBucketByKey(filterValue);
			if (elementBucket != null) {
				long docCount = getDocumentCount(elementBucket);
				facet.addEntry(filterValue, docCount);
				absDocCount += docCount;
			}
		}
		facet.setAbsoluteFacetCoverage(absDocCount);
	}

	private void fillFacet(Facet facet, Bucket facetNameBucket) {
		Terms facetValues = ((Terms) facetNameBucket.getAggregations().get(FACET_VALUES_AGG));
		long absDocCount = 0;
		for (Bucket valueBucket : facetValues.getBuckets()) {
			long docCount = getDocumentCount(valueBucket);
			facet.addEntry(valueBucket.getKeyAsString(), docCount);
			absDocCount += docCount;
		}
		facet.setAbsoluteFacetCoverage(absDocCount);
	}

	private long getDocumentCount(Bucket valueBucket) {
		long docCount = nestedFacetCorrector != null
				? nestedFacetCorrector.getCorrectedDocumentCount(valueBucket)
				: valueBucket.getDocCount();
		return docCount;
	}

}
