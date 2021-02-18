package de.cxp.ocs.smartsuggest.querysuggester;

import java.util.*;
import java.util.stream.Stream;

import de.cxp.ocs.smartsuggest.spi.SuggestDataProvider;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompoundQuerySuggester implements QuerySuggester {

	final List<QuerySuggester> suggesterList;

	@Setter
	private boolean isMultiThreaded = false;

	public CompoundQuerySuggester(List<QuerySuggester> suggester) {
		suggesterList = new ArrayList<>(suggester);
	}

	// for testing purposes
	CompoundQuerySuggester(String indexName, List<SuggestDataProvider> dataProviders, SuggesterFactory factory) {
		suggesterList = new ArrayList<>();
		for (SuggestDataProvider dataProvider : dataProviders) {
			if (dataProvider.hasData(indexName)) {
				QuerySuggester suggester = factory.getSuggester(dataProvider.loadData(indexName));
				suggesterList.add(suggester);
			}
		}
	}

	@Override
	public List<Suggestion> suggest(String term, int maxResults, Set<String> tags) throws SuggestException {
		if (suggesterList.isEmpty()) return Collections.emptyList();
		if (suggesterList.size() == 1) return suggesterList.get(0).suggest(term, maxResults, tags);

		Stream<QuerySuggester> suggesterStream = suggesterList.stream();
		if (isMultiThreaded) suggesterStream = suggesterStream.parallel();

		List<Suggestion> finalResult = new ArrayList<>();
		suggesterStream
				.map(s -> s.suggest(term, maxResults, tags))
				.forEach(finalResult::addAll);
		return finalResult;
	}

	@Override
	public boolean isReady() {
		return suggesterList.stream().allMatch(QuerySuggester::isReady);
	}

	@Override
	public void close() throws Exception {
		suggesterList.forEach(s -> {
			try {
				s.close();
			}
			catch (Exception e) {
				log.error("failed to close suggester {} because of ", s.getClass().getSimpleName(), e);
			}
		});

	}
}
