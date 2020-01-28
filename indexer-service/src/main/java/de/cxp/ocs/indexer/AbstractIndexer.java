package de.cxp.ocs.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.cxp.ocs.api.indexer.FullIndexationService;
import de.cxp.ocs.api.indexer.ImportSession;
import de.cxp.ocs.conf.IndexConfiguration;
import de.cxp.ocs.config.Field;
import de.cxp.ocs.indexer.model.IndexableItem;
import de.cxp.ocs.model.index.BulkImportData;
import de.cxp.ocs.model.index.Document;
import de.cxp.ocs.preprocessor.CombiFieldBuilder;
import de.cxp.ocs.preprocessor.DataPreProcessor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractIndexer implements FullIndexationService {

	@NonNull
	private final List<DataPreProcessor> dataPreProcessors;
	
	@Getter(value = AccessLevel.PROTECTED)
	@NonNull
	final IndexConfiguration indexConf;

	@Getter(value = AccessLevel.PROTECTED)
	final Map<String, Field> fields;

	private final CombiFieldBuilder combiFieldBuilder;

	private final IndexItemConverter indexItemConverter;

	public AbstractIndexer(@NonNull List<DataPreProcessor> dataPreProcessors, @NonNull IndexConfiguration indexConf) {
		this.dataPreProcessors = dataPreProcessors;
		this.indexConf = indexConf;
		fields = Collections.unmodifiableMap(indexConf.getFieldConfiguration().getFields());
		combiFieldBuilder = new CombiFieldBuilder(fields);
		indexItemConverter = new IndexItemConverter(fields);
	}

	@Override
	public ImportSession startImport(String indexName, String locale) throws IllegalStateException {
		if (isImportRunning(indexName)) {
			throw new IllegalStateException("Import for index " + indexName + " already running");
		}
		return new ImportSession(
				indexName,
				initNewIndex(indexName, locale));
	}

	public abstract boolean isImportRunning(String indexName);

	protected abstract String initNewIndex(String indexName, String locale);

	@Override
	public void add(BulkImportData data) throws Exception {
		validateSession(data.session);
		List<IndexableItem> bulk = new ArrayList<>();
		for (Document doc : data.getDocuments()) {
			// FIXME: document processors should work on indexable item
			// so they are able to modify only the usage dependent data, e.g.
			// only searchable data, instead also changing the result data!
			boolean isIndexable = preProcess(doc);
			if (isIndexable) bulk.add(indexItemConverter.toIndexableItem(doc));
		}
		if (bulk.size() > 0) {
			addToIndex(data.getSession(), bulk);
		}
	}

	protected abstract void addToIndex(ImportSession session, List<IndexableItem> bulk) throws Exception;

	private boolean preProcess(Document doc) {
		boolean isIndexable = true;

		combiFieldBuilder.build(doc);
		for (DataPreProcessor preProcessor : dataPreProcessors) {
			isIndexable = preProcessor.process(doc, isIndexable);
		}
		return isIndexable;
	}

	protected abstract void validateSession(ImportSession session) throws IllegalArgumentException;

	@Override
	public boolean done(ImportSession session) throws Exception {
		validateSession(session);
		return deploy(session);
	}

	protected abstract boolean deploy(ImportSession session);

	@Override
	public void cancel(ImportSession session) {
		validateSession(session);
		deleteIndex(session.temporaryIndexName);
	}

	protected abstract void deleteIndex(String indexName);

}