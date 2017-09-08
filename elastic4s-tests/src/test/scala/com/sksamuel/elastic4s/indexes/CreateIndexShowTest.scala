package com.sksamuel.elastic4s.indexes

import com.sksamuel.elastic4s.JsonSugar
import com.sksamuel.elastic4s.http.ElasticDsl
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class CreateIndexShowTest extends WordSpec with Matchers with JsonSugar with ElasticDsl {

  "CreateIndex" should {
    "have a show typeclass implementation" in {
      val req = {
        createIndex("gameofthrones").mappings(
          mapping("characters").fields(
            stringField("name"),
            stringField("location")
          ),
          mapping("locations").fields(
            stringField("name"),
            stringField("continent"),
            intField("iswinter")
            ) all true source true numericDetection false
        ) refreshInterval 10.seconds shards 4 replicas 2
      }

      CreateIndexShow.show(req) should matchJson("""{"settings":{"index":{"number_of_shards":4,"number_of_replicas":2,"refresh_interval":"10000ms"}},"mappings":{"characters":{"properties":{"name":{"type":"string"},"location":{"type":"string"}}},"locations":{"_all":{"enabled":true},"_source":{"enabled":true},"numeric_detection":false,"properties":{"name":{"type":"string"},"continent":{"type":"string"},"iswinter":{"type":"integer"}}}}}""")
    }
  }
}
