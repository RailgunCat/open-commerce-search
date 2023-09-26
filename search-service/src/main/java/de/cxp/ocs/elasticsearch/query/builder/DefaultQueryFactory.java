package de.cxp.ocs.elasticsearch.query.builder;

import java.util.Map;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import de.cxp.ocs.config.FieldConfigAccess;
import de.cxp.ocs.config.FieldConstants;
import de.cxp.ocs.config.QueryBuildingSetting;
import de.cxp.ocs.elasticsearch.model.query.ExtendedQuery;
import de.cxp.ocs.elasticsearch.query.MasterVariantQuery;
import de.cxp.ocs.spi.search.ESQueryFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * A predefined broad query that tries to get good results without loosing too
 * much precision.
 * </p>
 * <p>
 * No {@link QueryBuildingSetting}s are supported. Everything is predefined with
 * this query.
 * </p>
 * <p>
 * It should only be used with the main fields and their '.standard' subfield,
 * because it uses the standard analyzer.
 * </p>
 */
@RequiredArgsConstructor
public class DefaultQueryFactory implements ESQueryFactory {

	@Getter
	private String				name	= "defaultQuery";
	private Map<String, Float>	fields;
	private VariantQueryFactory	variantQueryFactory;

	@Override
	public void initialize(String name, Map<QueryBuildingSetting, String> settings, Map<String, Float> fieldWeights, FieldConfigAccess fieldConfig) {
		if (name != null) this.name = name;
		this.fields = fieldWeights;
		variantQueryFactory = new VariantQueryFactory(fieldConfig);
	}

	@Override
	public MasterVariantQuery createQuery(ExtendedQuery parsedQuery) {
		QueryStringQueryBuilder mainQuery = QueryBuilders
				.queryStringQuery(parsedQuery.toQueryString())
				.defaultField(FieldConstants.SEARCH_DATA + ".*")
				.analyzer("standard")
				.fuzziness(Fuzziness.AUTO)
				.minimumShouldMatch("2<80%")
				.tieBreaker(0.8f)
				.type(parsedQuery.getInputTerms().size() == 1 ? Type.BEST_FIELDS : Type.CROSS_FIELDS)
				.queryName(name);
		if (fields != null) {
			mainQuery.fields(fields);
		}

		QueryBuilder variantQuery = variantQueryFactory.createMatchAnyTermQuery(parsedQuery, fields);

		// isWithSpellCorrect=true because we use fuzzy matching
		return new MasterVariantQuery(mainQuery, variantQuery, true, false);
	}

	@Override
	public boolean allowParallelSpellcheckExecution() {
		return true;
	}
}
