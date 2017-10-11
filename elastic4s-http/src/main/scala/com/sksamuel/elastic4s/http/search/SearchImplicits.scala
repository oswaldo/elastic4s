package com.sksamuel.elastic4s.http.search

import java.net.URLEncoder

import cats.Show
import com.sksamuel.elastic4s.http.update.RequestFailure
import com.sksamuel.elastic4s.http.{HttpEntity, HttpExecutable, HttpRequestClient, HttpResponse, IndicesOptionsParams, ResponseHandler}
import com.sksamuel.elastic4s.json.JacksonSupport
import com.sksamuel.elastic4s.searches.queries.term.{BuildableTermsQuery, TermsQueryDefinition}
import com.sksamuel.elastic4s.searches.{MultiSearchDefinition, SearchDefinition, SearchType}
import com.sksamuel.exts.OptionImplicits._
import org.apache.http.entity.ContentType

import scala.concurrent.Future
import scala.util.Try



trait SearchImplicits {

  implicit def BuildableTermsNoOp[T]: BuildableTermsQuery[T] = new BuildableTermsQuery[T] {
    override def build(q: TermsQueryDefinition[T]): Any = null // not used by the http builders
  }

  implicit object SearchShow extends Show[SearchDefinition] {
    override def show(req: SearchDefinition): String = SearchBodyBuilderFn(req).string()
  }

  implicit object MultiSearchShow extends Show[MultiSearchDefinition] {
    override def show(req: MultiSearchDefinition): String = MultiSearchBuilderFn(req)
  }

  implicit object MultiSearchHttpExecutable extends HttpExecutable[MultiSearchDefinition, MultiSearchResponse] {

    import scala.collection.JavaConverters._

    override def responseHandler: ResponseHandler[MultiSearchResponse] = new ResponseHandler[MultiSearchResponse] {
      override def handle(response: HttpResponse): Try[MultiSearchResponse] = Try {
        val json = JacksonSupport.mapper.readTree(response.entity.get.content)
        val items = json.get("responses").elements.asScala.zipWithIndex.map { case (element, index) =>
          val status = element.get("status").intValue()
          val either = if (element.has("error"))
            Left(JacksonSupport.mapper.treeToValue[SearchError](element))
          else
            Right(JacksonSupport.mapper.treeToValue[SearchResponse](element))
          MultisearchResponseItem(index, status, either)
        }.toSeq
        MultiSearchResponse(items)
      }
    }

    override def execute(client: HttpRequestClient, request: MultiSearchDefinition): Future[HttpResponse] = {

      val params = scala.collection.mutable.Map.empty[String, String]
      request.maxConcurrentSearches.map(_.toString).foreach(params.put("max_concurrent_searches", _))

      val body = MultiSearchBuilderFn(request)
      logger.debug("Executing msearch: " + body)
      val entity = HttpEntity(body, ContentType.APPLICATION_JSON.getMimeType)
      client.async("POST", "/_msearch", params.toMap, entity)
    }
  }

  implicit object SearchHttpExecutable extends HttpExecutable[SearchDefinition, Either[RequestFailure, SearchResponse]] {

    override def responseHandler = new ResponseHandler[Either[RequestFailure, SearchResponse]] {
      override def doit(response: HttpResponse): Either[RequestFailure, SearchResponse] = response.statusCode match {
        case 200 =>
          val entity = response.entity.getOrError("No entity defined")
          Right(ResponseHandler.fromEntity[SearchResponse](entity).copy(json = entity.content))
        case _ => Left(ResponseHandler.fromEntity[RequestFailure](response.entity.get))
      }
    }

    override def execute(client: HttpRequestClient, request: SearchDefinition): Future[HttpResponse] = {

      val endpoint = if (request.indexesTypes.indexes.isEmpty && request.indexesTypes.types.isEmpty)
        "/_search"
      else if (request.indexesTypes.indexes.isEmpty)
        "/_all/" + request.indexesTypes.types.map(URLEncoder.encode).mkString(",") + "/_search"
      else if (request.indexesTypes.types.isEmpty)
        "/" + request.indexesTypes.indexes.map(URLEncoder.encode).mkString(",") + "/_search"
      else
        "/" + request.indexesTypes.indexes.map(URLEncoder.encode).mkString(",") + "/" + request.indexesTypes.types.map(URLEncoder.encode).mkString(",") + "/_search"

      val params = scala.collection.mutable.Map.empty[String, String]
      request.requestCache.map(_.toString).foreach(params.put("request_cache", _))
      request.searchType.filter(_ != SearchType.DEFAULT).map(SearchTypeHttpParameters.convert).foreach(params.put("search_type", _))
      request.control.routing.map(_.toString).foreach(params.put("routing", _))
      request.keepAlive.foreach(params.put("scroll", _))

      request.indicesOptions.foreach { opts =>
        IndicesOptionsParams(opts).foreach { case (key, value) => params.put(key, value) }
      }

      val builder = SearchBodyBuilderFn(request)
      val body = builder.string()

      client.async("POST", endpoint, params.toMap, HttpEntity(body, ContentType.APPLICATION_JSON.getMimeType))
    }
  }
}
