package com.stadiamaps.autocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiamaps.api.apis.GeocodingApi
import com.stadiamaps.api.models.PeliasGeoJSONFeature
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.await

@OptIn(ExperimentalCoroutinesApi::class)
class AutoCompleteViewModel(private val service: GeocodingApi) : ViewModel() {
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  private val _suggestions = MutableStateFlow<List<PeliasGeoJSONFeature>>(emptyList())
  val suggestions: StateFlow<List<PeliasGeoJSONFeature>> = _suggestions.asStateFlow()

  private val _isActive = MutableStateFlow(false)
  val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    viewModelScope.launch {
      _query
          //                .debounce(100) // TODO: Debounce to limit network calls?
          //                .filter { it.isNotEmpty() }
          //                .distinctUntilChanged()
          .flatMapLatest { query -> fetchSuggestions(query, search = false) }
          .collect { suggestions -> _suggestions.value = suggestions }
    }
  }

  fun onQueryChanged(newQuery: String) {
    _query.value = newQuery
  }

  /** Search when pressing the IME search button; do a deeper search. */
  fun onSearch(query: String) {
    fetchSuggestions(query, search = true)
  }

  fun onActiveChange(newActive: Boolean) {
    _isActive.value = newActive
  }

  fun onFeatureClicked(feature: PeliasGeoJSONFeature) {
    // TODO: Do we want to do anything else here?
    onActiveChange(false)
  }

  // TODO: Is this flow necessary?
  private fun fetchSuggestions(query: String, search: Boolean): Flow<List<PeliasGeoJSONFeature>> =
      flow {
            // TODO: Other filter parameters
            // TODO: Caching of last N queries?
            if (query.isBlank()) {
              emit(listOf())
              _isLoading.value = false
              return@flow
            }

            _isLoading.value = true
            val results =
                if (search) {
                  service.search(text = query).await().features
                } else {
                  service.autocomplete(text = query).await().features
                }

            // Avoid emitting updates when the query is stale (more typing)!
            if (_query.value == query) {
              emit(results)
            }
            _isLoading.value = false
          }
          .catch { _isLoading.value = false }
}
