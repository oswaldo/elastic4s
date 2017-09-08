package com.sksamuel.elastic4s.http.search.queries

import com.sksamuel.elastic4s.http.search.queries.nested.InnerHitQueryBodyFn
import com.sksamuel.elastic4s.searches.HighlightFieldDefinition
import com.sksamuel.elastic4s.searches.queries.InnerHitDefinition
import com.sksamuel.elastic4s.searches.sort.FieldSortDefinition
import org.scalatest.{FunSuite, Matchers}

class InnerHitQueryBodyFnTest extends FunSuite with Matchers {

  test("inner hit should generate expected json") {
    val q = InnerHitDefinition("inners")
      .from(2)
      .explain(false)
      .trackScores(true)
      .version(true)
      .size(2)
      .docValueFields(List("df1", "df2"))
      .sortBy(FieldSortDefinition("sortField"))
      .storedFieldNames(List("field1", "field2"))
      .highlighting(HighlightFieldDefinition("hlField"))

    InnerHitQueryBodyFn(q).string() shouldBe
      """{"name":"inners","from":2,"explain":false,"track_scores":true,"version":true,"size":2,"docvalue_fields":["df1","df2"],"sort":[{"sortField":{"order":"asc"}}],"stored_fields":["field1","field2"],"highlight":{"fields":{"hlField":{}}}}"""
  }
}
