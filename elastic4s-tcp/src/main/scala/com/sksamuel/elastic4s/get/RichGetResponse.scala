package com.sksamuel.elastic4s.get

import java.util

import com.sksamuel.elastic4s.Hit
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.common.document.DocumentField

import scala.collection.JavaConverters._

case class RichGetResponse(original: GetResponse) extends Hit {

  // java method aliases
  @deprecated("use .java", "5.0.0")
  def getField(name: String): DocumentField = original.getField(name)

  @deprecated("use sourceAsMap", "5.0.0")
  def getFields: util.Map[String, DocumentField] = original.getFields

  @deprecated("use .java", "5.0.0")
  def getId: String = id

  @deprecated("use .java", "5.0.0")
  def getIndex: String = index

  @deprecated("use .java", "5.0.0")
  def getType: String = `type`

  @deprecated("use .java", "5.0.0")
  def getVersion: Long = version

  @deprecated("use .exists", "5.0.0")
  def isExists: Boolean = exists

  override def score: Float = 0

  override def id: String = original.getId
  override def index: String = original.getIndex
  override def `type`: String = original.getType
  override def version: Long = original.getVersion

  private def getFieldToHitField(f: DocumentField) = new HitField {
    override def name: String = f.getName
    override def value: AnyRef = f.getValue
    override def values: Seq[AnyRef] = Option(f.getValues).map(_.asScala).getOrElse(Nil)
    override def isMetadataField: Boolean = f.isMetadataField
  }

  @deprecated("use sourceField instead", "5.0.0")
  def field(name: String): HitField = getFieldToHitField(original.getField(name))

  @deprecated("use sourceFieldOpt instead", "5.0.0")
  def fieldOpt(name: String): Option[HitField] = Option(original.getField(name)).map(getFieldToHitField)

  @deprecated("use sourceAsMap instead", "5.0.0")
  def fields: Map[String, HitField] = {
    Option(original.getFields).fold(Map.empty[String, HitField])(_.asScala.toMap.mapValues(getFieldToHitField))
  }

  @deprecated("use .sourceAsMap", "5.0.0")
  def source: Map[String, AnyRef] = sourceAsMap

  override def sourceAsMap: Map[String, AnyRef] = Option(original.getSource).map(_.asScala.toMap).getOrElse(Map.empty)
  override def sourceAsString: String = original.getSourceAsString

  override def exists: Boolean = original.isExists

  @deprecated("Use the source methods instead", "5.0.0")
  def iterator: Iterator[DocumentField] = original.iterator.asScala
}


