package com.sksamuel.elastic4s.searches.queries.funcscorer

import com.sksamuel.elastic4s.searches.queries.QueryDefinition
import com.sksamuel.exts.OptionImplicits._

case class GaussianDecayScoreDefinition(field: String,
                                        origin: String,
                                        scale: String,
                                        offset: Option[Any] = None,
                                        decay: Option[Double] = None,
                                        weight: Option[Double] = None,
                                        multiValueMode: Option[MultiValueMode] = None,
                                        override val filter: Option[QueryDefinition] = None) extends ScoreFunctionDefinition {

  def multiValueMode(multiValueMode: MultiValueMode): GaussianDecayScoreDefinition =
    copy(multiValueMode = multiValueMode.some)

  def weight(weight: Double): GaussianDecayScoreDefinition = copy(weight = weight.some)
  def decay(decay: Double): GaussianDecayScoreDefinition = copy(decay = decay.some)
  def offset(offset: Any): GaussianDecayScoreDefinition = copy(offset = offset.some)
  def filter(filter: QueryDefinition): GaussianDecayScoreDefinition = copy(filter = filter.some)
}
