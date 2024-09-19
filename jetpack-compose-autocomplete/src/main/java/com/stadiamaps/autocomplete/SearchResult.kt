package com.stadiamaps.autocomplete

import android.icu.util.ULocale
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.stadiamaps.api.models.GeoJSONPoint
import com.stadiamaps.api.models.PeliasGeoJSONFeature
import com.stadiamaps.api.models.PeliasGeoJSONProperties
import com.stadiamaps.api.models.PeliasLayer

@Composable
fun SearchResult(
    feature: PeliasGeoJSONFeature,
    modifier: Modifier = Modifier,
    relativeTo: Location? = null,
    localeOverride: ULocale? = null,
    distanceMeasurementSystemOverride: DistanceMeasurementSystem? = null,
    iconTintColor: Color = MaterialTheme.colorScheme.onBackground,
) {
  val distanceFormatter =
      remember(localeOverride, distanceMeasurementSystemOverride) {
        LocalizedDistanceFormatter(localeOverride, distanceMeasurementSystemOverride)
      }

  ListItem(
      headlineContent = { Text(text = feature.properties?.name ?: "<No Info>") },
      modifier = modifier,
      supportingContent = {
        feature.properties?.subtitle()?.let { subtitle -> Text(text = subtitle) }
      },
      leadingContent = {
        val layer = feature.properties?.layer
        if (layer != null) {
          Icon(painterResource(layer.icon()), "$layer icon", tint = iconTintColor)
        }
      },
      trailingContent = {
        val featureLocation = feature.center()
        if (relativeTo != null && featureLocation != null) {
          val distance = relativeTo.distanceTo(featureLocation)
          val formattedDistance = distanceFormatter.format(distance.toDouble())

          Text(text = formattedDistance)
        }
      })
}

@Preview("Plain result", showBackground = true)
@Composable
fun PlainResultPreview() {
  SearchResult(
      PeliasGeoJSONFeature(
          PeliasGeoJSONFeature.Type.Feature,
          GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
          bbox = listOf(0.0, 0.0, 0.0, 0.0),
          properties = PeliasGeoJSONProperties(layer = PeliasLayer.address, name = "Test")))
}

@Preview("Result with locality", showBackground = true)
@Composable
fun ResultWithLocalityPreview() {
  SearchResult(
      PeliasGeoJSONFeature(
          PeliasGeoJSONFeature.Type.Feature,
          GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
          bbox = listOf(0.0, 0.0, 0.0, 0.0),
          properties =
              PeliasGeoJSONProperties(
                  layer = PeliasLayer.address, name = "Test", locality = "Some City")))
}

@Preview("Relative distance", showBackground = true)
@Composable
fun RelativeDistancePreview() {
  SearchResult(
      PeliasGeoJSONFeature(
          PeliasGeoJSONFeature.Type.Feature,
          GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
          bbox = listOf(0.0, 0.0, 0.0, 0.0),
          properties = PeliasGeoJSONProperties(layer = PeliasLayer.address, name = "Test")),
      relativeTo =
          Location("").apply {
            latitude = 0.25
            longitude = 0.25
          })
}

@Preview("Multiple results", showBackground = true)
@Composable
fun MultipleResultsPreview() {
  Column {
    SearchResult(
        PeliasGeoJSONFeature(
            PeliasGeoJSONFeature.Type.Feature,
            GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
            bbox = listOf(0.0, 0.0, 0.0, 0.0),
            properties =
                PeliasGeoJSONProperties(layer = PeliasLayer.address, name = "123 Some Street")),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })

    SearchResult(
        PeliasGeoJSONFeature(
            PeliasGeoJSONFeature.Type.Feature,
            GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
            bbox = listOf(0.0, 0.0, 0.0, 0.0),
            properties = PeliasGeoJSONProperties(layer = PeliasLayer.street, name = "Some Street")),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })

    SearchResult(
        PeliasGeoJSONFeature(
            PeliasGeoJSONFeature.Type.Feature,
            GeoJSONPoint(GeoJSONPoint.Type.Point, listOf(0.0, 0.0)),
            bbox = listOf(0.0, 0.0, 0.0, 0.0),
            properties = PeliasGeoJSONProperties(layer = PeliasLayer.venue, name = "Some Cafe")),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })
  }
}
