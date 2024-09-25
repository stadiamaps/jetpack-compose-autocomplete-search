package com.stadiamaps.autocomplete

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiamaps.api.apis.GeocodingApi
import com.stadiamaps.api.infrastructure.CollectionFormats
import com.stadiamaps.api.models.PeliasGeoJSONFeature
import com.stadiamaps.api.models.PeliasLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.await

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AutoCompleteViewModel(private val service: GeocodingApi) : ViewModel() {
  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  private val _suggestions = MutableStateFlow<List<PeliasGeoJSONFeature>>(emptyList())
  val suggestions: StateFlow<List<PeliasGeoJSONFeature>> = _suggestions.asStateFlow()

  private val _isActive = MutableStateFlow(false)
  val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  /**
   * The minimum length of a query text.
   *
   * While a filter of ~3 may make sense for many languages (ex: English), it definitely doesn't for
   * others (ex: Chinese, Japanese, or Korean).
   */
  var minSearchLength = 0

  /** Waits between subsequent searches until at least this interval has passed. */
  var debounceInterval = 300L

  /**
   * If set, biases search near a specific location and displays the distance from the user in the
   * search results.
   */
  var userLocation: Location? = null

  /** Optionally limits the searched layers to the specified set. */
  var limitLayers: List<PeliasLayer>? = null

  init {
    viewModelScope.launch(Dispatchers.IO) {
      _query
          .debounce(debounceInterval)
          .map { it.trim() }
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
    onActiveChange(false)
  }

  private fun fetchSuggestions(query: String, search: Boolean): Flow<List<PeliasGeoJSONFeature>> =
      flow {
            // TODO: Caching of last N queries?
            if (query.count() < minSearchLength) {
              emit(listOf())
              _isLoading.value = false
              return@flow
            }

            val layers = limitLayers?.map { it.value }?.let { CollectionFormats.CSVParams(it) }

            _isLoading.value = true
            val results =
                if (search) {
                  service
                      .search(
                          text = query,
                          focusPointLat = userLocation?.latitude,
                          focusPointLon = userLocation?.longitude,
                          layers = layers)
                      .await()
                      .features
                } else {
                  service
                      .autocomplete(
                          text = query,
                          focusPointLat = userLocation?.latitude,
                          focusPointLon = userLocation?.longitude,
                          layers = layers)
                      .await()
                      .features
                }

            // Avoid emitting updates when the query is stale (more typing)!
            if (_query.value == query) {
              emit(results)
            }
            _isLoading.value = false
          }
          .catch { _isLoading.value = false }
}
