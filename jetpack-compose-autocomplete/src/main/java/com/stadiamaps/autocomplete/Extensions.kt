package com.stadiamaps.autocomplete

import android.location.Location
import com.stadiamaps.api.models.AddressComponentsV2
import com.stadiamaps.api.models.Context
import com.stadiamaps.api.models.FeaturePropertiesV2
import com.stadiamaps.api.models.FeaturePropertiesV2Properties
import com.stadiamaps.api.models.GeocodingGeoJSONFeature
import com.stadiamaps.api.models.GeocodingGeoJSONProperties
import com.stadiamaps.api.models.Point
import com.stadiamaps.api.models.Precision
import com.stadiamaps.api.models.WofContext
import com.stadiamaps.api.models.WofContextComponent

fun FeaturePropertiesV2.center(): Location? =
    if (geometry != null && geometry?.type == "Point") {
      Location("").apply {
        latitude = geometry!!.coordinates[1]
        longitude = geometry!!.coordinates[0]
      }
    } else {
      bbox?.let { bbox ->
        Location("").apply {
          latitude = (bbox[1] + bbox[3]) / 2
          longitude = (bbox[0] + bbox[2]) / 2
        }
      }
    }

fun String.layerIcon(): Int =
    when (this) {
      "venue" -> R.drawable.baseline_location_pin_24
      "address" -> R.drawable.baseline_123_24
      "street" -> R.drawable.rounded_road_24
      "postalcode" -> R.drawable.rounded_local_post_office_24
      "locality",
      "localadmin",
      "borough",
      "neighbourhood",
      "coarse",
      "macrohood" -> R.drawable.baseline_location_city_24
      "country",
      "macroregion",
      "region",
      "macrocounty",
      "county",
      "dependency",
      "disputed" -> R.drawable.outline_globe_uk_24
      "empire",
      "continent" -> R.drawable.outline_language_24
      "marinearea",
      "ocean" -> R.drawable.baseline_waves_24
      else -> R.drawable.baseline_location_pin_24
    }

// Temporary conversions which can go away once we launch v2 search

fun GeocodingGeoJSONProperties.subtitle(): String =
    when (layer) {
          "venue",
          "poi",
          "address",
          "street",
          "neighbourhood",
          "postalcode",
          "macrohood" -> listOf(locality ?: region, country)
          "country",
          "dependency",
          "disputed" -> listOf(continent)
          "macroregion",
          "region" -> listOf(country)
          "macrocounty",
          "county",
          "locality",
          "localadmin",
          "borough" -> listOf(region, country)
          "coarse",
          "marinearea",
          "empire",
          "continent",
          "ocean",
          null -> listOf(layer.toString())
          else -> listOf(layer.toString())
        }
        .filterNotNull()
        .joinToString()

fun contextComponent(
    gid: String?,
    name: String?,
    abbreviation: String? = null
): WofContextComponent? =
    if (gid != null && name != null) {
      WofContextComponent(gid, name, abbreviation)
    } else {
      null
    }

fun GeocodingGeoJSONFeature.upcast(): FeaturePropertiesV2? {
  val props = properties ?: return null
  return FeaturePropertiesV2(
      bbox = bbox,
      geometry = Point(geometry.coordinates, "Point"),
      properties =
          FeaturePropertiesV2Properties(
              gid = props.gid!!,
              layer =
                  when (props.layer) {
                    "venue" -> "poi"
                    else -> props.layer!!
                  },
              name = props.name!!,
              precision =
                  if (props.accuracy == GeocodingGeoJSONProperties.Accuracy.point) {
                    Precision.point
                  } else {
                    Precision.centroid
                  },
              addressComponents =
                  AddressComponentsV2(
                      number = props.housenumber,
                      postalCode = props.postalcode,
                      street = props.street),
              coarseLocation = props.subtitle(),
              confidence = props.confidence,
              context =
                  Context(
                      whosonfirst =
                          WofContext(
                              borough = contextComponent(props.boroughGid, props.borough),
                              continent = contextComponent(props.continentGid, props.continent),
                              country =
                                  contextComponent(props.countryGid, props.country, props.countryA),
                              county = contextComponent(props.countyGid, props.county),
                              locality = contextComponent(props.localityGid, props.locality),
                              neighbourhood =
                                  contextComponent(props.neighbourhoodGid, props.neighbourhood))),
              distance = null,
          ),
      type = "Feature")
}
