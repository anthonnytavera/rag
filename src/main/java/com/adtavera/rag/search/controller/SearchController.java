package com.adtavera.rag.search.controller;

import java.util.List;

import com.adtavera.rag.search.dto.SearchResultDto;
import com.adtavera.rag.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Step 5: the retrieval part of RAG on its own, with no LLM involved.
 */
@RestController
@Tag(name = "4. Search", description = "Búsqueda semántica sobre el documento indexado")
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/search")
	@Operation(summary = "Buscar los chunks más parecidos a una consulta",
			description = "Genera el embedding de la consulta y devuelve los top-K chunks más parecidos "
					+ "de la base vectorial, con su score. No usa el modelo de chat de Gemini.")
	public List<SearchResultDto> search(
			@Parameter(description = "Lo que buscas", example = "¿cuántos días de vacaciones tengo?")
			@RequestParam String query) {
		return searchService.search(query);
	}
}
