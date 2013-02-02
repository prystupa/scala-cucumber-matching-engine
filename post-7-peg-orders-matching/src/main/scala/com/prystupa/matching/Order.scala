package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:59 AM
 */

sealed trait Order {
  val broker: String
  val qty: Double
  val side: Side

  def withQty(qty: Double): Order
}

case class LimitOrder(broker: String, side: Side, qty: Double, limit: Double) extends Order {
  def withQty(qty: Double): LimitOrder = copy(qty = qty)
}

case class MarketOrder(broker: String, side: Side, qty: Double) extends Order {
  def withQty(qty: Double): MarketOrder = copy(qty = qty)
}

case class PegOrder(broker: String, side: Side, qty: Double, limit: Option[Double]) extends Order {
  def withQty(qty: Double): PegOrder = copy(qty = qty)
}
