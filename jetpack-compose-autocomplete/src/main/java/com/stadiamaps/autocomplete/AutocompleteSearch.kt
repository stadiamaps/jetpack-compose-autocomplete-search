package com.stadiamaps.autocomplete

import android.content.res.Resources
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stadiamaps.api.GeocodingApi
import com.stadiamaps.api.auth.ApiKeyAuth
import com.stadiamaps.api.infrastructure.ApiClient
import com.stadiamaps.api.infrastructure.ApiClient.Companion.defaultBasePath
import com.stadiamaps.api.models.FeaturePropertiesV2
import com.stadiamaps.api.models.LayerId
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient

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
 *   save API credits, but setting it higher than 1 or 2 will make the autocomplete functionality
 *   significantly less useful for some languages.
 * @param debounceInterval Waits between subsequent searches until at least this interval
 *   (milliseconds) has passed.
 * @param onFeatureClicked An optional callback invoked when a result is tapped in the list.
 * @param resultView An optional composable which replaces the default search result view. Note that
 *   you are expected to use the modifier as it configures interactivity.
 */
@ExperimentalMaterial3Api
@Composable
fun AutocompleteSearch(
    modifier: Modifier = Modifier,
    apiKey: String,
    useEuEndpoint: Boolean = false,
    userLocation: Location? = null,
    limitLayers: List<LayerId>? = null,
    minSearchLength: Int = 1,
    debounceInterval: Long = 300,
    resultView: @Composable ((FeaturePropertiesV2, Modifier) -> Unit)? = null,
    onFeatureClicked: (FeaturePropertiesV2) -> Unit = {},
) {
  val service =
      remember(apiKey, useEuEndpoint) {
        val baseUrl =
            if (useEuEndpoint) {
              "https://api-eu.stadiamaps.com"
            } else {
              defaultBasePath
            }
        val client =
            ApiClient(
                baseUrl = baseUrl,
                okHttpClientBuilder =
                    OkHttpClient.Builder()
                        .addInterceptor(
                            Interceptor { chain: Interceptor.Chain ->
                              // Add the accept-language header
                              val original = chain.request()
                              val newRequest =
                                  original
                                      .newBuilder()
                                      .header(
                                          "Accept-Language",
                                          Resources.getSystem()
                                              .configuration
                                              .locales
                                              .toLanguageTags())
                                      .build()
                              chain.proceed(newRequest)
                            }))
        client.addAuthorization("ApiKeyAuth", ApiKeyAuth("query", "api_key", apiKey))
        client.createService(GeocodingApi::class.java)
      }

  val viewModel: AutoCompleteViewModel = viewModel { AutoCompleteViewModel(service) }
  val coroutineScope = rememberCoroutineScope()

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
        query = query.first,
        onQueryChanged = viewModel::onQueryChanged,
        onSearch = viewModel::onSearch,
        active = isActive,
        onActiveChange = viewModel::onActiveChange,
    ) {
      if (isActive) {
        SuggestionsDropdown(
            suggestions = suggestions,
            resultView = { feature ->
              val clickModifier =
                  Modifier.clickable {
                    coroutineScope.launch {
                      val detailFeature = viewModel.onFeatureClicked(feature)
                      onFeatureClicked(detailFeature)
                    }
                  }

              if (resultView != null) {
                resultView(feature, clickModifier)
              } else {
                SearchResult(feature, modifier = clickModifier, relativeTo = userLocation)
              }
            },
            isLoading = isLoading,
        )
      }
    }
  }
}
