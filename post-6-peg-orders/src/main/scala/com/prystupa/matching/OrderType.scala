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

  def all(buy: => OrderBook, sell: => OrderBook): PartialFunction[Order, OrderType] = {

    case self@LimitOrder(_, side, _, limit) => new OrderType {
      def bookDisplay: String = limit.toString

      def price: PriceLevel = LimitPrice(limit)

      def crossesAt(price: Double): Boolean = side match {
        case Buy => price <= limit
        case Sell => price >= limit
      }

      def decreasedBy(qty: Double): LimitOrder = self.copy(qty = self.qty - qty)
    }

    case self@MarketOrder(_, _, _) => new OrderType {
      def bookDisplay: String = "MO"

      def price: PriceLevel = MarketPrice

      def crossesAt(price: Double): Boolean = true

      def decreasedBy(qty: Double): MarketOrder = self.copy(qty = self.qty - qty)
    }

    case self@PegOrder(_, side, _) => new OrderType {
      private lazy val book = side match {
        case Buy => buy
        case Sell => sell
      }

      def bookDisplay: String = s"Peg($bestLimit)"

      def price: PriceLevel = PegPrice

      def crossesAt(price: Double): Boolean = side match {
        case Buy => price <= bestLimit
        case Sell => price >= bestLimit
      }

      def decreasedBy(qty: Double): Order = self.copy(qty = self.qty - qty)

      private def bestLimit = book.bestLimit match {
        case Some(limit) => limit
        case None => throw new IllegalStateException("Pegged orders can't be placed in a book without best limit")
      }
    }
  }
}
