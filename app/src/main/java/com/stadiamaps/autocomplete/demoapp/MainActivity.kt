package com.stadiamaps.autocomplete.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.stadiamaps.autocomplete.AutocompleteSearch
import com.stadiamaps.autocomplete.center
import com.stadiamaps.autocomplete.demoapp.ui.theme.StadiaMapsAutocompleteSearchTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val apiKey = BuildConfig.stadiaApiKey

    enableEdgeToEdge()
    setContent {
      StadiaMapsAutocompleteSearchTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AutocompleteSearch(
              modifier = Modifier.padding(innerPadding),
              apiKey = apiKey,
          ) {
            println("Selected ${it.properties?.name} at ${it.center()}")
          }
        }
      }
    }
  }
}
