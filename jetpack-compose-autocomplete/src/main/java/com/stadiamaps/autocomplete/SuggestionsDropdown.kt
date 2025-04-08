package com.stadiamaps.autocomplete

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
import com.stadiamaps.api.models.FeaturePropertiesV2

@Composable
fun SuggestionsDropdown(
    suggestions: List<FeaturePropertiesV2>,
    isLoading: Boolean,
    resultView: @Composable ((FeaturePropertiesV2) -> Unit),
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState())) {
    if (isLoading) {
      Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
      }
    }
    suggestions.forEach { feature -> resultView(feature) }
  }
}
