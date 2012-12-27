package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:59 AM
 */

trait Order {
  val broker: String
  val qty: Double
  val side: Side

  def crossesAt(price: Double): Boolean

  def decreasedBy(qty: Double): Order

  def bookDisplay: String
}

case class LimitOrder(broker: String, side: Side, qty: Double, limit: Double) extends Order {

  override def crossesAt(price: Double): Boolean = side match {
    case Buy => price <= limit
    case Sell => price >= limit
  }

  override def decreasedBy(qty: Double): LimitOrder =
    LimitOrder(broker, side, this.qty - qty, limit)

  override val bookDisplay = limit.toString
}

case class MarketOrder(broker: String, side: Side, qty: Double) extends Order {

  override def crossesAt(price: Double): Boolean = true

  override def decreasedBy(qty: Double): MarketOrder =
    MarketOrder(broker, side, this.qty - qty)

  override val bookDisplay = "MO"
}