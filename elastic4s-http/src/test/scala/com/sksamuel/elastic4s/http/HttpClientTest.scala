package com.sksamuel.elastic4s.http

import java.net.{SocketException, UnknownHostException}

import com.sksamuel.elastic4s.ElasticsearchClientUri
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration.Duration

class HttpClientTest extends FlatSpec with Matchers with ElasticDsl {

  "HttpClient" should "throw an error when it cannot connect" in {
    intercept[SocketException] {
      val client = HttpClient(ElasticsearchClientUri("123", 1))
      executeCall(client)
    }
  }

  it should "throw an error when the host is unknown" in {
    intercept[UnknownHostException] {
      val client = HttpClient(ElasticsearchClientUri("someUnknownHost123", 1))
      executeCall(client)
    }
  }

  it should "throw an error when the uri is invalid" in {
    intercept[RuntimeException] {
      val client = HttpClient(ElasticsearchClientUri("1:2-3", 1))
      executeCall(client)
    }
  }

  def executeCall(client: HttpClient) = {
    client.execute {
      indexInto("a-index" / "a-type") id "a-id" fields Map("wibble" -> "foo")
    }.await(Duration.Inf)
  }
}
