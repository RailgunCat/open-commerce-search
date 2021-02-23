package de.cxp.ocs.smartsuggest.querysuggester;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import de.cxp.ocs.smartsuggest.spi.SuggestRecord;

public interface QueryIndexer {

	/**
	 * @return The time of the last successful indexing
	 */
	Instant getLastIndexTime();

	/**
	 * 
	 * @param suggestions
	 *        the suggestions to index
	 * @return future that is ready as soon as the indexation is done
	 * @throws IOException
	 *         in case indexation fails
	 */
	CompletableFuture<Void> index(Iterable<SuggestRecord> suggestions) throws IOException;
}
