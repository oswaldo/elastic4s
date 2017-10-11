package com.sksamuel.elastic4s

import org.scalatest.{FlatSpec, Matchers}

class ElasticsearchClientUriTest extends FlatSpec with Matchers {

  private def testString(connectionString: String,
                         hosts: List[ElasticsearchNode],
                         options: Map[String, String] = Map.empty): Unit = {
    val uri = ElasticsearchClientUri(connectionString)
    uri.hosts shouldBe hosts
    uri.options shouldBe options
  }

  "elasticsearch uri" should "parse multiple host/ports" in {
    testString("elasticsearch://host1:1234,host2:2345", List(ElasticsearchNode("host1", 1234), ElasticsearchNode("host2", 2345)))
  }

  it should "parse single host/ports" in {
    testString("elasticsearch://host1:1234", List(ElasticsearchNode("host1", 1234)))
  }

  it should "parse single host/ports with trailing slash" in {
    testString("elasticsearch://host1:1234/", List(ElasticsearchNode("host1", 1234)))
  }

  it should "errors on trailing commas" in {
    intercept[RuntimeException] {
      testString("elasticsearch://host1:1234,", List(ElasticsearchNode("host1", 1234)))
    }
  }

  it should "parse everything" in {
    testString("elasticsearch://host1:1234,host2:9999?a=b&c=d",
      List(ElasticsearchNode("host1", 1234), ElasticsearchNode("host2", 9999)),
      Map("a" -> "b", "c" -> "d"))
  }

  it should "parse everything with trailing slash" in {
    testString("elasticsearch://host1:1234,host2:9999/?a=b&c=d",
      List(ElasticsearchNode("host1", 1234), ElasticsearchNode("host2", 9999)),
      Map("a" -> "b", "c" -> "d"))
  }

  it should "parse options" in {
    ElasticsearchClientUri("elasticsearch://host1:1234,host2:9999?a=b&c=d").options shouldBe Map("a" -> "b", "c" -> "d")
  }

  it should "error on missing values between commas" in {
    intercept[RuntimeException] {
      ElasticsearchClientUri("elasticsearch://host1:1234,,host2:9999")
    } should not be null
  }
}
