package com.stadiamaps.autocomplete

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stadiamaps.api.apis.GeocodingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.infrastructure.ApiClient.Companion.defaultBasePath
import com.stadiamaps.api.models.PeliasGeoJSONFeature
import com.stadiamaps.api.models.PeliasLayer

/**
 * An autocomplete search view that finds geographic locations as you type.
 *
 * @param modifier Composable view modifiers.
 * @param apiKey Your Stadia Maps API key (see https://docs.stadiamaps.com/authentication/).
 * @param useEuEndpoint Send requests to servers located in the European Union (may significantly
 *   degrade performance outside Europe).
 * @param userLocation If present, biases the search for results near a specific location and
 *   displays results with (straight-line) distances from this location.
 * @param limitLayers Optionally limits the searched layers to the specified set.
 * @param minSearchLength Requires at least this many characters of input before searching. This can
 *   save API credits, but setting it more than 1 or 2 will be significantly less useful for many
 *   languages.
 * @param debounceInterval Waits between subsequent searches until at least this interval
 *   (milliseconds) has passed.
 * @param onFeatureClicked An optional callback invoked when a result is tapped in the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteSearch(
    modifier: Modifier = Modifier,
    apiKey: String,
    useEuEndpoint: Boolean = false,
    userLocation: Location? = null,
    limitLayers: List<PeliasLayer>? = null,
    minSearchLength: Int = 0,
    debounceInterval: Long = 300,
    onFeatureClicked: (PeliasGeoJSONFeature) -> Unit = {},
    // TODO: Configurable language
    // TODO: Composable result view builder
) {
  val service =
      remember(apiKey, useEuEndpoint) {
        val baseUrl =
            if (useEuEndpoint) {
              "https://api-eu.stadiamaps.com"
            } else {
              defaultBasePath
            }
        val client = ApiClient(baseUrl = baseUrl)
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        client.createService(GeocodingApi::class.java)
      }

  val viewModel: AutoCompleteViewModel = viewModel { AutoCompleteViewModel(service) }

  viewModel.userLocation = userLocation
  viewModel.minSearchLength = minSearchLength
  viewModel.debounceInterval = debounceInterval
  viewModel.limitLayers = limitLayers

  val query by viewModel.query.collectAsState()
  val suggestions by viewModel.suggestions.collectAsState()
  val isActive by viewModel.isActive.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  Column(modifier = modifier) {
    AutoCompleteSearchBar(
        query = query,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::onSearch,
        active = isActive,
        onActiveChange = viewModel::onActiveChange,
    ) {
      if (isActive) {
        SuggestionsDropdown(
            suggestions = suggestions,
            onFeatureClicked = { feature ->
              viewModel.onFeatureClicked(feature)
              onFeatureClicked(feature)
            },
            isLoading = isLoading,
        )
      }
    }
  }
}
