package com.sksamuel.elastic4s

import org.scalatest.{FlatSpec, Matchers}

class ElasticsearchClientUriTest extends FlatSpec with Matchers {

  "ElasticsearchClientUri" should "correcly parse a simple uri" in {
    val uriString = """elasticsearch://host:123"""
    val clientUri = ElasticsearchClientUri(uriString)
    clientUri shouldBe ElasticsearchClientUri(
      uri = uriString,
      hosts = ElasticsearchNode("host", 123) :: Nil,
      pathPrefix = None,
      options = Map.empty)
  }

  it should "correcly parse a complex uri" in {
    val uriString = """elasticsearch://host:123,192.168.3.1:456/pathPrefix?option=value&other=v"""
    val clientUri = ElasticsearchClientUri(uriString)
    clientUri shouldBe ElasticsearchClientUri(
        uri = uriString,
        hosts = ElasticsearchNode("host", 123) :: ElasticsearchNode("192.168.3.1", 456) :: Nil,
        pathPrefix = Some("/pathPrefix"),
        options = Map("option" -> "value", "other" -> "v"))
  }

}
