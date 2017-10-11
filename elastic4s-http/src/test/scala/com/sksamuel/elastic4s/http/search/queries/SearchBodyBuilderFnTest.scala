package com.sksamuel.elastic4s.http.search.queries

import com.sksamuel.elastic4s.DistanceUnit
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn
import com.sksamuel.elastic4s.searches.GeoPoint
import com.sksamuel.elastic4s.searches.queries.geo.GeoDistanceQueryDefinition
import com.sksamuel.elastic4s.searches.sort.{GeoDistanceSortDefinition, SortOrder}
import org.scalatest.{FunSuite, Matchers}
import com.sksamuel.exts.OptionImplicits._

class SearchBodyBuilderFnTest extends FunSuite with Matchers {

  test("highlight with 'matchedMatchedFields' generates proper 'matched_fields' field as array field.") {
    val request = search("example" / "1") highlighting {
      highlight("text")
      .matchedFields("text", "text.ngram", "text.japanese")
    }
    SearchBodyBuilderFn(request).string() shouldBe
      """{"version":true,"highlight":{"fields":{"text":{"matched_fields":["text","text.ngram","text.japanese"]}}}}"""
  }
  test("highlight with 'highlighterType' generates 'type' field.") {
    val request = search("example" / "1") highlighting {
      highlight("text")
        .highlighterType("fvh")
    }
    SearchBodyBuilderFn(request).string() shouldBe
      """{"version":true,"highlight":{"fields":{"text":{"type":"fvh"}}}}"""
  }
  test("highlight with 'boundaryChars' generates 'boundary_chars' field.") {
    val request = search("example" / "1") highlighting {
      highlight("text")
        .boundaryChars("test")
    }
    SearchBodyBuilderFn(request).string() shouldBe
      """{"version":true,"highlight":{"fields":{"text":{"boundary_chars":"test"}}}}"""
  }
  test("geo distance query with sort") {

    val geoDistanceQueryDefinition = GeoDistanceQueryDefinition(
      field = "location",
      point = Some(43.65435, -79.38871),
      distanceStr = "100km".some
    )

    val req = search("partner-location") limit 100 query geoDistanceQueryDefinition sortBy GeoDistanceSortDefinition(
      field = "location",
      points = Seq(GeoPoint(43.65435, -79.38871)),
      order = Some(SortOrder.ASC),
      unit = Some(DistanceUnit.KILOMETERS)
    )

    SearchBodyBuilderFn(req).string shouldBe
      """{"version":true,"query":{"geo_distance":{"distance":"100km","location":[-79.38871,43.65435]}},"size":100,"sort":[{"_geo_distance":{"location":[[-79.38871,43.65435]],"order":"asc"}}]}"""
  }
}
