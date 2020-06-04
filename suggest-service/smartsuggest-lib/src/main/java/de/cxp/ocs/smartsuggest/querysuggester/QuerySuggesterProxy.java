package de.cxp.ocs.smartsuggest.querysuggester;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.lucene.store.AlreadyClosedException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuerySuggesterProxy implements QuerySuggester {

	private final AtomicReference<QuerySuggester>	innerQuerySuggester			= new AtomicReference<>(new NoQuerySuggester());
	private final String							indexName;
	private volatile boolean						isClosed					= false;
	private int										maxSuggestionsPerCacheEntry	= 10;

	private final int					cacheLetterLength	= Integer.getInteger("CACHE_LETTER_LENGTH", 3);
	private Cache<String, List<Result>>	firstLetterCache	= CacheBuilder.newBuilder()
			.maximumSize(Long.getLong("CACHE_MAX_SIZE", 10_000L))
			.build();

	public QuerySuggesterProxy(String indexName) {
		this.indexName = indexName;
	}

	public QuerySuggesterProxy(String indexName, int maxSuggestionsPerCacheEntry) {
		this(indexName);
		this.maxSuggestionsPerCacheEntry = maxSuggestionsPerCacheEntry;
	}

	public void updateQueryMapper(@NonNull QuerySuggester newSuggester) throws AlreadyClosedException {
		if (isClosed) throw new AlreadyClosedException("suggester for tenant " + indexName + " closed");
		if (log.isDebugEnabled()) {
			log.debug("updating from {} to {} for tenant {}",
					innerQuerySuggester.get().getClass().getSimpleName(),
					newSuggester.getClass().getSimpleName(),
					indexName);
		}
		firstLetterCache.asMap().keySet()
				.forEach(term -> firstLetterCache.put(term, newSuggester.suggest(term)));
		QuerySuggester oldSuggester = innerQuerySuggester.getAndSet(newSuggester);
		if (oldSuggester != null) {
			oldSuggester.destroy();
		}
	}

	@Override
	public void close() throws Exception {
		isClosed = true;
		firstLetterCache.invalidateAll();
		firstLetterCache.cleanUp();
		innerQuerySuggester.get().close();
	}

	@Override
	public List<Result> suggest(String term, int maxResults, Set<String> groups) throws SuggestException {
		if (isClosed || isBlank(term)) return emptyList();
		String normalizedTerm = term.trim().toLowerCase();

		// only cache results, if no filter is given and the teh maximum amount
		// of results is <= to the maxSuggestionPerCacheEntry level
		if (normalizedTerm.length() <= cacheLetterLength && (groups == null || groups.isEmpty()) && maxResults <= maxSuggestionsPerCacheEntry) {
			try {
				List<Result> cachedResults = firstLetterCache.get(normalizedTerm, () -> innerQuerySuggester.get().suggest(normalizedTerm, maxSuggestionsPerCacheEntry, groups));

				if (maxResults < maxSuggestionsPerCacheEntry) {
					cachedResults = truncateResult(maxResults, cachedResults);
				}

				return cachedResults;
			}
			catch (ExecutionException e) {
				throw new SuggestException(e.getCause());
			}
		}
		else {
			return innerQuerySuggester.get().suggest(normalizedTerm, maxResults, groups);
		}
	}

	private List<Result> truncateResult(int maxResults, List<Result> cachedResults) {
		List<Result> truncatedResults = new ArrayList<>();
		int resultCount = 0;
		for (Result r : cachedResults) {
			if (r.getSuggestions().size() > resultCount + maxResults) {
				truncatedResults.add(new Result(r.getName(), r.getSuggestions().subList(0, maxResults - resultCount)));
				resultCount = maxResults;
				break;
			}
			else {
				truncatedResults.add(r);
				resultCount += r.getSuggestions().size();
			}
		}
		cachedResults = truncatedResults;
		return cachedResults;
	}
}
