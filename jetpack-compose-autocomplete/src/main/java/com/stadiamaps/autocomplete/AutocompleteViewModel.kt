package com.stadiamaps.autocomplete

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.infrastructure.CollectionFormats
import com.stadiamaps.api.models.FeaturePropertiesV2
import com.stadiamaps.api.models.LayerId
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
  private val _query = MutableStateFlow("" to false)
  val query: StateFlow<Pair<String, Boolean>> = _query.asStateFlow()

  private val _suggestions = MutableStateFlow<List<FeaturePropertiesV2>>(emptyList())
  val suggestions: StateFlow<List<FeaturePropertiesV2>> = _suggestions.asStateFlow()

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
  var minSearchLength = 1

  /** Waits between subsequent searches until at least this interval has passed. */
  var debounceInterval = 300L

  /**
   * If set, biases search near a specific location and displays the distance from the user in the
   * search results.
   */
  var userLocation: Location? = null

  /** Optionally limits the searched layers to the specified set. */
  var limitLayers: List<LayerId>? = null

  init {
    viewModelScope.launch(Dispatchers.IO) {
      _query
          .debounce(debounceInterval)
          .map { (text, search) -> text.trim() to search }
          .flatMapLatest { (text, search) -> fetchSuggestions(text, search = search) }
          .collect { suggestions -> _suggestions.value = suggestions }
    }
  }

  fun onQueryChanged(newQuery: String) {
    _query.value = newQuery to false
  }

  /** Search when pressing the IME search button; do a deeper search. */
  fun onSearch(query: String) {
    _query.value = query to true
  }

  fun onActiveChange(newActive: Boolean) {
    _isActive.value = newActive
  }

  suspend fun onFeatureClicked(feature: FeaturePropertiesV2): FeaturePropertiesV2 {
    onActiveChange(false)

    if (feature.geometry != null) {
      return feature
    } else {
      // Look up the feature using the place details API
      return service.placeDetailsV2(CollectionFormats.CSVParams(feature.properties.gid)).await().features.first()
    }
  }

  private fun fetchSuggestions(query: String, search: Boolean): Flow<List<FeaturePropertiesV2>> =
      flow {
            // TODO: Caching of last N queries?
            if (query.isBlank() || query.count() < minSearchLength) {
              emit(listOf())
            } else {
              val layers = limitLayers?.map { it.value }?.let { CollectionFormats.CSVParams(it) }

              _isLoading.value = true
              val results =
                  try {
                    if (search) {
                      service
                          .search(
                              text = query,
                              focusPointLat = userLocation?.latitude,
                              focusPointLon = userLocation?.longitude,
                              layers = layers)
                          .await()
                          .features
                        .mapNotNull { it.upcast() }
                    } else {
                      service
                          .autocompleteV2(
                              text = query,
                              focusPointLat = userLocation?.latitude,
                              focusPointLon = userLocation?.longitude,
                              layers = layers)
                          .await()
                          .features
                    }
                  } catch (e: Throwable) {
                    _isLoading.value = false

                    // These failures won't be easily visible deep in a composable; bubble them up
                    // here.
                    Log.e("AutocompleteViewModel", "Error loading autocomplete results: $e")
                    throw e
                  }

              // Avoid emitting updates when the query is stale (more typing)!
              if (_query.value == query to search) {
                emit(results)
              }
            }
            _isLoading.value = false
          }
          .catch { _isLoading.value = false }
}
