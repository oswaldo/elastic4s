package com.sksamuel.elastic4s.streams

import java.util.concurrent.{CountDownLatch, TimeUnit}

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.http.search.SearchHit
import com.sksamuel.elastic4s.indexes.IndexDefinition
import com.sksamuel.elastic4s.searches.RichSearchHit
import com.sksamuel.elastic4s.testkit.{DiscoveryLocalNodeProvider, HttpElasticSugar}
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.{Matchers, WordSpec}

class ScrollPublisherIntegrationTest extends WordSpec with DiscoveryLocalNodeProvider with Matchers with ElasticDsl with HttpElasticSugar {

  import ReactiveElastic._
  import com.sksamuel.elastic4s.jackson.ElasticJackson.Implicits._

  private val indexName = getClass.getSimpleName.toLowerCase
  private val indexType = "emperor"

  implicit val system = ActorSystem()

  val emperors = Array(
    Item("Augustus"),
    Item("Tiberius"),
    Item("Caligua"),
    Item("Claudius"),
    Item("Nero"),
    Item("Galba"),
    Item("Otho"),
    Item("Vitellius"),
    Item("Vespasian"),
    Item("Titus"),
    Item("Domitian"),
    Item("Nerva"),
    Item("Trajan"),
    Item("Hadrian"),
    Item("Antoninus Pius"),
    Item("Marcus Aurelius"),
    Item("Commodus"),
    Item("Pertinax"),
    Item("Diocletion")
  )

  implicit object RichSearchHitRequestBuilder extends RequestBuilder[RichSearchHit] {
    override def request(hit: RichSearchHit): IndexDefinition = {
      indexInto(indexName / indexType).doc(hit.sourceAsString)
    }
  }

  ensureIndexExists(indexName)

  http.execute {
    bulk(emperors.map(indexInto(indexName / indexType).source(_)))
  }.await

  blockUntilCount(emperors.length, indexName)

  "elastic-streams" should {
    "publish all data from the index" in {

      val publisher = http.publisher(search(indexName) query "*:*" scroll "1m")

      val completionLatch = new CountDownLatch(1)
      val documentLatch = new CountDownLatch(emperors.length)

      publisher.subscribe(new Subscriber[SearchHit] {
        override def onComplete(): Unit = completionLatch.countDown()
        override def onError(t: Throwable): Unit = fail(t)
        override def onSubscribe(s: Subscription): Unit = s.request(1000)
        override def onNext(t: SearchHit): Unit = documentLatch.countDown()
      })

      completionLatch.await(10, TimeUnit.SECONDS) shouldBe true
      documentLatch.await(10, TimeUnit.SECONDS) shouldBe true
    }
  }
}
