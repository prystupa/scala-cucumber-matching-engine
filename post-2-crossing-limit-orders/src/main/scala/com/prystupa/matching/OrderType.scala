package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/2/13
 * Time: 9:37 PM
 */
trait OrderType {
  def bookDisplay: String

  def price: PriceLevel

  def crossesAt(price: Double): Boolean

  def decreasedBy(qty: Double): Order
}

object OrderType {

  def all(): PartialFunction[Order, OrderType] = {

    case self@LimitOrder(_, side, _, limit) => new OrderType {
      def bookDisplay: String = limit.toString

      def price: PriceLevel = LimitPrice(limit)

      def crossesAt(price: Double): Boolean = side match {
        case Buy => price <= limit
        case Sell => price >= limit
      }

      def decreasedBy(qty: Double): LimitOrder =
        self.copy(qty = self.qty - qty)
    }
  }
}