package com.sksamuel.elastic4s.http.cat

import com.sksamuel.elastic4s.cat._
import com.sksamuel.elastic4s.http.{DefaultResponseHandler, HttpExecutable, HttpRequestClient, HttpResponse, ResponseHandler}

import scala.concurrent.Future
import scala.util.Try

trait CatImplicits {

  implicit object CatShardsExecutable extends HttpExecutable[CatShardsDefinition, Seq[CatShards]] {
    override def execute(client: HttpRequestClient, request: CatShardsDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/shards?v&format=json&bytes=b", Map.empty)
    }
  }

  implicit object CatNodesExecutable extends HttpExecutable[CatNodesDefinition, Seq[CatNodes]] {
    override def execute(client: HttpRequestClient, request: CatNodesDefinition): Future[HttpResponse] = {
      val headers = Seq(
        "id", "pid", "ip", "port", "http_address", "version", "build", "jdk", "disk.avail", "heap.current", "heap.percent", "heap.max", "ram.current", "ram.percent", "ram.max", "file_desc.current", "file_desc.percent", "file_desc.max", "cpu", "load_1m", "load_5m", "load_15m", "uptime", "node.role", "master", "name", "completion.size", "fielddata.memory_size", "fielddata.evictions", "query_cache.memory_size", "query_cache.evictions", "request_cache.memory_size", "request_cache.evictions", "request_cache.miss_count", "flush.total"
      ).mkString(",")
      client.async("GET", s"/_cat/nodes?v&h=$headers&format=json", Map.empty)
    }
  }

  implicit object CatPluginsExecutable extends HttpExecutable[CatPluginsDefinition, Seq[CatPlugin]] {
    override def execute(client: HttpRequestClient, request: CatPluginsDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/plugins?v&format=json", Map.empty)
    }
  }

  implicit object CatThreadPoolExecutable extends HttpExecutable[CatThreadPoolDefinition, Seq[CatThreadPool]] {
    override def execute(client: HttpRequestClient, request: CatThreadPoolDefinition): Future[HttpResponse] = {
      val headers = "id,name,active,rejected,completed,type,size,queue,queue_size,largest,min,max,keep_alive,node_id,ephemeral_id,pid,host,ip,port"
      client.async("GET", s"/_cat/thread_pool?v&format=json&h=$headers", Map.empty)
    }
  }

  implicit object CatHealthExecutable extends HttpExecutable[CatHealthDefinition, CatHealth] {

    override def responseHandler: ResponseHandler[CatHealth] = new DefaultResponseHandler[CatHealth] {
      override def handle(response: HttpResponse): Try[CatHealth] = Try {
        ResponseHandler.fromEntity[Seq[CatHealth]](response.entity.get).head
      }
    }

    override def execute(client: HttpRequestClient, request: CatHealthDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/health?v&format=json", Map.empty)
    }
  }

  implicit object CatCountExecutable extends HttpExecutable[CatCountDefinition, CatCount] {

    override def responseHandler: ResponseHandler[CatCount] = new DefaultResponseHandler[CatCount] {
      override def handle(response: HttpResponse): Try[CatCount] = Try {
        ResponseHandler.fromEntity[Seq[CatCount]](response.entity.get).head
      }
    }

    override def execute(client: HttpRequestClient, request: CatCountDefinition): Future[HttpResponse] = {
      val endpoint = request.indices match {
        case Nil => "/_cat/count?v&format=json"
        case indexes => "/_cat/count/" + indexes.mkString(",") + "?v&format=json"
      }
      client.async("GET", endpoint, Map.empty)
    }
  }

  implicit object CatMasterExecutable extends HttpExecutable[CatMasterDefinition, CatMaster] {

    override def responseHandler: ResponseHandler[CatMaster] = new DefaultResponseHandler[CatMaster] {
      override def handle(response: HttpResponse): Try[CatMaster] = Try {
        ResponseHandler.fromEntity[Seq[CatMaster]](response.entity.get).head
      }
    }

    override def execute(client: HttpRequestClient, request: CatMasterDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/master?v&format=json", Map.empty)
    }
  }

  implicit object CatAliasesExecutable extends HttpExecutable[CatAliasesDefinition, Seq[CatAlias]] {
    override def execute(client: HttpRequestClient, request: CatAliasesDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/aliases?v&format=json", Map.empty)
    }
  }

  implicit object CatIndexesExecutable extends HttpExecutable[CatIndexesDefinition, Seq[CatIndices]] {

    val BaseEndpoint = "/_cat/indices?v&format=json&bytes=b"

    override def execute(client: HttpRequestClient, request: CatIndexesDefinition): Future[HttpResponse] = {
      val endpoint = request.health match {
        case Some(health) => BaseEndpoint + "&health=" + health.getClass.getSimpleName.toLowerCase.stripSuffix("$")
        case _ => BaseEndpoint
      }
      client.async("GET", endpoint, Map.empty)
    }
  }

  implicit object CatAllocationExecutable extends HttpExecutable[CatAllocationDefinition, Seq[CatAllocation]] {

    override def execute(client: HttpRequestClient, request: CatAllocationDefinition): Future[HttpResponse] = {
      client.async("GET", "/_cat/aliases?v&format=json&bytes=b", Map.empty)
    }
  }
}
