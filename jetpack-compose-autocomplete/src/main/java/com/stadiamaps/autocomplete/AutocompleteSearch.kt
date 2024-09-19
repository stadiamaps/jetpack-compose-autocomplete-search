package com.stadiamaps.autocomplete

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
import com.stadiamaps.api.models.PeliasGeoJSONFeature

// TODO: Configurables! A LOT of them!
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteSearch(
    modifier: Modifier = Modifier,
    apiKey: String,
    onFeatureClicked: (PeliasGeoJSONFeature) -> Unit,
) {
  val service =
      remember(apiKey) {
        val client = ApiClient()
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        client.createService(GeocodingApi::class.java)
      }

  val viewModel: AutoCompleteViewModel = viewModel { AutoCompleteViewModel(service) }

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
