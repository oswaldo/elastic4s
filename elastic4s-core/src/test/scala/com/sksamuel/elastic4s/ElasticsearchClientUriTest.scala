package com.sksamuel.elastic4s

import org.scalatest.{FlatSpec, Matchers}

class ElasticsearchClientUriTest extends FlatSpec with Matchers {

  "ElasticsearchClientUri" should "correcly parse a complex uri" in {
    val clientUri = ElasticsearchClientUri("""elasticsearch://host:123/a,192.168.3.1:456?option=value""")
    clientUri.hosts shouldBe ElasticsearchNode("host", 123, Some("/a")) :: ElasticsearchNode("192.168.3.1", 456, None) :: Nil
  }
}
