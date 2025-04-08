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
import com.stadiamaps.api.models.FeaturePropertiesV2
import com.stadiamaps.api.models.FeaturePropertiesV2Properties
import com.stadiamaps.api.models.Point
import com.stadiamaps.api.models.Precision

@Composable
fun SearchResult(
    feature: FeaturePropertiesV2,
    modifier: Modifier = Modifier,
    // TODO: Legacy; we can remove this once we have v2 search
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
      headlineContent = { Text(text = feature.properties.name) },
      modifier = modifier,
      supportingContent = {
        feature.properties.coarseLocation?.let { subtitle -> Text(text = subtitle) }
      },
      leadingContent = {
        val layer = feature.properties.layer
        Icon(painterResource(layer.layerIcon()), "$layer icon", tint = iconTintColor)
      },
      trailingContent = {
        val apiDistance = feature.properties.distance
        val featureLocation = feature.center()
        if (apiDistance != null) {
          val formattedDistance = distanceFormatter.format(apiDistance * 1000.0)
          Text(text = formattedDistance)
        } else if (relativeTo != null && featureLocation != null) {
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
      FeaturePropertiesV2(
          properties =
              FeaturePropertiesV2Properties(
                  layer = "address",
                  name = "Test",
                  gid = "foo:bar:123",
                  precision = Precision.point)))
}

@Preview("Result with locality", showBackground = true)
@Composable
fun ResultWithLocalityPreview() {
  SearchResult(
      FeaturePropertiesV2(
          properties =
              FeaturePropertiesV2Properties(
                  layer = "address",
                  name = "Test",
                  gid = "foo:bar:123",
                  precision = Precision.point,
                  coarseLocation = "Some City, USA")))
}

@Preview("Relative distance", showBackground = true)
@Composable
fun RelativeDistancePreview() {
  SearchResult(
      FeaturePropertiesV2(
          properties =
              FeaturePropertiesV2Properties(
                  layer = "address",
                  name = "Test",
                  gid = "foo:bar:123",
                  precision = Precision.point,
                  coarseLocation = "Some City, USA",
                  distance = 12.0)))
}

@Preview("Multiple results", showBackground = true)
@Composable
fun MultipleResultsPreview() {
  Column {
    SearchResult(
        FeaturePropertiesV2(
            properties =
                FeaturePropertiesV2Properties(
                    layer = "address",
                    name = "123 some street",
                    gid = "foo:bar:123",
                    precision = Precision.point,
                    coarseLocation = "Some City, USA",
                    distance = 12.0)),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })

    // Legacy for v1 search: should calculate relative distance internally
    SearchResult(
        FeaturePropertiesV2(
            properties =
                FeaturePropertiesV2Properties(
                    layer = "street",
                    name = "Some Street",
                    gid = "foo:bar:456",
                    precision = Precision.point,
                    coarseLocation = "Some City, USA"),
            geometry = Point(listOf(0.0, 0.0), "Point")),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })

    SearchResult(
        FeaturePropertiesV2(
            properties =
                FeaturePropertiesV2Properties(
                    layer = "poi",
                    name = "Some Cafe",
                    gid = "foo:bar:789",
                    precision = Precision.point,
                    coarseLocation = "Some City, USA",
                    distance = 16.4)),
        relativeTo =
            Location("").apply {
              latitude = 0.25
              longitude = 0.25
            })
  }
}
