package com.stadiamaps.autocomplete

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stadiamaps.api.models.PeliasGeoJSONFeature

@Composable
fun SuggestionsDropdown(
    suggestions: List<PeliasGeoJSONFeature>,
    onFeatureClicked: (PeliasGeoJSONFeature) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    relativeTo: Location? = null,
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    if (isLoading) {
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
      }
    }
    suggestions.forEach { feature ->
      // TODO: Locale customizations
      SearchResult(
          feature,
          modifier = Modifier.clickable { onFeatureClicked(feature) },
          relativeTo = relativeTo)
    }
  }
}
