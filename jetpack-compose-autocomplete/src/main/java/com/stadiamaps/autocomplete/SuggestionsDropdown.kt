package com.stadiamaps.autocomplete

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stadiamaps.api.models.PeliasGeoJSONFeature

@Composable
fun SuggestionsDropdown(
    suggestions: List<PeliasGeoJSONFeature>,
    onFeatureClicked: (PeliasGeoJSONFeature) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    suggestions.forEach { feature ->
      // TODO: Customizations! A lot of them!
      SearchResult(feature, modifier = Modifier.clickable { onFeatureClicked(feature) })
    }
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.padding(8.dp))
    }
  }
}
