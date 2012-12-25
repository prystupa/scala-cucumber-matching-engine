package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:59 AM
 */

trait Side

case object Buy extends Side

case object Sell extends Side

trait Order {
  val broker: String
  val qty: Double
  val side: Side

  def decreasedBy(qty: Double): Order

  def typeString: String
}

case class LimitOrder(broker: String, side: Side, qty: Double, limit: Double) extends Order {

  override def decreasedBy(qty: Double): LimitOrder = LimitOrder(broker, side, this.qty - qty, limit)

  def typeString = limit.toString
}
