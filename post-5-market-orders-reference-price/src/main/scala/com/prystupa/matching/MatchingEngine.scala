package com.prystupa.matching

import collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:14 PM
 */

class MatchingEngine(buy: OrderBook, sell: OrderBook, orderTypes: (Order => OrderType))
  extends mutable.Publisher[OrderBookEvent] {

  private var _referencePrice: Option[Double] = None

  def referencePrice = _referencePrice.get

  def referencePrice_=(price: Double) {
    _referencePrice = Some(price)
  }

  def acceptOrder(order: Order) {

    val (book, counterBook) = getBooks(order.side)
    val unfilledOrder = tryMatch(order, counterBook)
    unfilledOrder.foreach(book.add(_))
  }


  private def getBooks(side: Side): (OrderBook, OrderBook) = side match {
    case Buy => (buy, sell)
    case Sell => (sell, buy)
  }

  private def tryMatch(order: Order, counterBook: OrderBook): Option[Order] = {

    if (order.qty == 0) None
    else counterBook.top match {
      case None => Some(order)
      case Some(top) => tryMatchWithTop(order, top) match {
        case None => Some(order)
        case Some(trade) => {
          counterBook.decreaseTopBy(trade.qty)
          publish(trade)
          val unfilledOrder = orderTypes(order).decreasedBy(trade.qty)
          tryMatch(unfilledOrder, counterBook)
        }
      }
    }
  }

  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = {
    def trade(price: Double) = {
      _referencePrice = Some(price)
      val (buy, sell) = if (order.side == Buy) (order, top) else (top, order)
      Some(Trade(buy.broker, sell.broker, price, math.min(buy.qty, sell.qty)))
    }

    lazy val oppositeBestLimit = {
      val oppositeBook = if (order.side == Buy) sell else buy
      oppositeBook.bestLimit
    }

    (order, top) match {

      case (_, topLimitOrder: LimitOrder) => {
        if (orderTypes(order).crossesAt(topLimitOrder.limit)) trade(topLimitOrder.limit)
        else None
      }

      case (limitOrder: LimitOrder, _: MarketOrder) => trade(oppositeBestLimit match {
        case Some(limit) => if (orderTypes(limitOrder).crossesAt(limit)) limit else limitOrder.limit
        case None => limitOrder.limit
      })

      case (_: MarketOrder, _: MarketOrder) => trade(oppositeBestLimit match {
        case Some(limit) => limit
        case None => _referencePrice match {
          case Some(price) => price
          case None => throw new IllegalStateException("Can't execute a trade with two market orders without best limit or reference price")
        }
      })
    }
  }
}
