package de.cxp.ocs.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.cxp.ocs.api.indexer.UpdateIndexService;
import de.cxp.ocs.model.index.Document;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/indexer-api/v1/update/{indexName}")
public class UpdateIndexController implements UpdateIndexService {

	@Autowired
	private IndexerCache indexerManager;

	@PatchMapping
	@Override
	public boolean patchDocument(@PathVariable("indexName") String indexName, @RequestBody Document doc) {
		try {
			return indexerManager.getIndexer(indexName).update(indexName, doc);
		}
		catch (ExecutionException e) {
			log.error("failed to get indexer", e);
			throw new RuntimeException(e);
		}
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PutMapping
	@Override
	public boolean putDocument(
			@PathVariable("indexName") String indexName, 
			@RequestParam(name = "replaceExisting", defaultValue = "false") Boolean replaceExisting, 
			@RequestBody Document doc) {
		try {
			return indexerManager.getIndexer(indexName).putDocument(indexName, replaceExisting, doc);
		}
		catch (ExecutionException e) {
			log.error("failed to get indexer", e);
			throw new RuntimeException(e);
		}
	}

	@DeleteMapping
	@Override
	public boolean deleteDocument(@PathVariable("indexName") String indexName, @RequestParam("id") String id) {
		try {
			return indexerManager.getIndexer(indexName).delete(indexName, id);
		}
		catch (ExecutionException e) {
			log.error("failed to get indexer", e);
			throw new RuntimeException(e);
		}
	}

}
