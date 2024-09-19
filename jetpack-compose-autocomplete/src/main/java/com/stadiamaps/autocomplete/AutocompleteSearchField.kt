package com.stadiamaps.autocomplete

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AutoCompleteSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
  TextField(
      value = query,
      onValueChange = onQueryChanged,
      modifier = modifier.fillMaxWidth(),
      placeholder = { Text(text = placeholder) },
      singleLine = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    content: @Composable() (ColumnScope.() -> Unit)
) {
  DockedSearchBar(
      inputField = {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChanged,
            onSearch = onSearch,
            expanded = active,
            onExpandedChange = onActiveChange,
            placeholder = { Text(text = placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
      },
      expanded = active,
      onExpandedChange = onActiveChange,
      modifier = modifier.fillMaxWidth(),
      colors = colors,
      content = content,
  )
}
