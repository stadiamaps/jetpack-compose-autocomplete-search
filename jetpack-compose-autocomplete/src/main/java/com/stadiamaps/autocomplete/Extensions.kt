package com.stadiamaps.autocomplete

import android.location.Location
import com.stadiamaps.api.models.PeliasGeoJSONFeature
import com.stadiamaps.api.models.PeliasGeoJSONProperties
import com.stadiamaps.api.models.PeliasLayer

fun PeliasGeoJSONFeature.center(): Location? =
    bbox?.let { bbox ->
      Location("").apply {
        latitude = (bbox[1] + bbox[3]) / 2
        longitude = (bbox[0] + bbox[2]) / 2
      }
    }

fun PeliasLayer.icon(): Int =
    when (this) {
      PeliasLayer.venue -> R.drawable.baseline_location_pin_24
      PeliasLayer.address -> R.drawable.baseline_123_24
      PeliasLayer.street -> R.drawable.rounded_road_24
      PeliasLayer.postalcode -> R.drawable.rounded_local_post_office_24
      PeliasLayer.locality,
      PeliasLayer.localadmin,
      PeliasLayer.borough,
      PeliasLayer.neighbourhood,
      PeliasLayer.coarse,
      PeliasLayer.macrohood -> R.drawable.baseline_location_city_24
      PeliasLayer.country,
      PeliasLayer.macroregion,
      PeliasLayer.region,
      PeliasLayer.macrocounty,
      PeliasLayer.county,
      PeliasLayer.dependency,
      PeliasLayer.disputed -> R.drawable.outline_globe_uk_24
      PeliasLayer.empire,
      PeliasLayer.continent -> R.drawable.outline_language_24
      PeliasLayer.marinearea,
      PeliasLayer.ocean -> R.drawable.baseline_waves_24
    }

fun PeliasGeoJSONProperties.subtitle(): String =
    when (layer) {
          PeliasLayer.venue,
          PeliasLayer.address,
          PeliasLayer.street,
          PeliasLayer.neighbourhood,
          PeliasLayer.postalcode,
          PeliasLayer.macrohood -> listOf(locality ?: region, country)
          PeliasLayer.country,
          PeliasLayer.dependency,
          PeliasLayer.disputed -> listOf(continent)
          PeliasLayer.macroregion,
          PeliasLayer.region -> listOf(country)
          PeliasLayer.macrocounty,
          PeliasLayer.county,
          PeliasLayer.locality,
          PeliasLayer.localadmin,
          PeliasLayer.borough -> listOf(region, country)
          PeliasLayer.coarse,
          PeliasLayer.marinearea,
          PeliasLayer.empire,
          PeliasLayer.continent,
          PeliasLayer.ocean,
          null -> listOf(layer.toString())
        }
        .filterNotNull()
        .joinToString()
