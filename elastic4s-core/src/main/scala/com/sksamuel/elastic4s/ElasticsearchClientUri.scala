package com.sksamuel.elastic4s

import com.sksamuel.exts.StringOption

import scala.language.implicitConversions

object ElasticsearchClientUri {

  val HostAndPortPattern = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]):(\\d+)"
  private val HostListPattern = s"(($HostAndPortPattern)(,$HostAndPortPattern)*)"
  private val PathPattern = s"(\\/(([^\\/]+).*)?)?"
  private val UriPattern = s"^elasticsearch:\\/\\/$HostListPattern$PathPattern(\\?(.*))$$"
  private val UriRegex = UriPattern.r

  implicit def stringtoUri(str: String): ElasticsearchClientUri = ElasticsearchClientUri(str)

  /**
    * Creates an ElasticsearchClientUri from a single host and port with no options.
    */
  def apply(host: String, port: Int): ElasticsearchClientUri = apply(s"elasticsearch://$host:$port")

  def apply(str: String): ElasticsearchClientUri = {
    UriRegex.findFirstMatchIn(str) match {
      case Some(m) => {
        val hoststr = m.group(1)
        val pathPrefix = Option(m.group(12))
        val query = StringOption(m.group(16))
        val hosts = hoststr.split(',').map(_.split(':')).map {
          case Array(host, port) => {
            (host, port.toInt)
          }
          case _ => sys.error(s"Invalid hosts/ports $hoststr")
        }
        val options = query
          .map(_.split('&')).getOrElse(Array.empty)
          .map(_.split('=')).collect {
            case Array(key, value) => (key, value)
            case _ => sys.error(s"Invalid query $query")
          }
        ElasticsearchClientUri(str, hosts.map { case (host, port) => ElasticsearchNode(host, port) }.toList, pathPrefix, options.toMap)
      }
      case None => sys.error(s"Invalid uri $str, must be in format elasticsearch://host:port,host:port/pathPrefix?querystring")
    }
  }
}

case class ElasticsearchNode(host: String, port: Int)

/**
  * Uri used to connect to an Elasticsearch cluster. The general format is
  *
  * elasticsearch://host:port,host:port/pathPrefix?querystring
  *
  * Multiple host:port combinations can be specified, seperated by commas.
  * Options can be specified using standard uri query string syntax, eg cluster.name=superman
  *
  * To use HTTPS when using the HTTP client, add ssl=true to the query parameters.
  *
  */
case class ElasticsearchClientUri(uri: String, hosts: List[ElasticsearchNode], pathPrefix: Option[String], options: Map[String, String] = Map.empty)
