package com.prystupa.matching

import collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:14 PM
 */

class MatchingEngine(buy: OrderBook, sell: OrderBook) extends mutable.Publisher[OrderBookEvent] {

  private var _referencePrice: Option[Double] = None

  def referencePrice = _referencePrice.get

  def referencePrice_=(price: Double) {
    _referencePrice = Some(price)
  }

  def acceptOrder(order: Order) {

    val (book, counterBook) = getBooks(order.side)
    counterBook.modify(ops => {
      val unfilledOrder = tryMatch(order, ops)
      unfilledOrder.foreach(book.add(_))
    })
  }


  private def getBooks(side: Side): (OrderBook, OrderBook) = side match {
    case Buy => (buy, sell)
    case Sell => (sell, buy)
  }

  private def tryMatch(order: Order, counterBook: OrderBook.ModifyOperations): Option[Order] = {

    if (order.qty == 0) None
    else counterBook.top match {
      case None => Some(order)
      case Some(top) => tryMatchWithTop(order, top) match {
        case None => Some(order)
        case Some(trade) => {
          counterBook.decreaseTopBy(trade.qty)
          publish(trade)
          val unfilledOrder = order.withQty(order.qty - trade.qty)
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

    def crosses(limit: Double, price: Double) = order.side match {
      case Buy => price <= limit
      case Sell => price >= limit
    }

    val (book, oppositeBook) = getBooks(order.side)

    (book.valueOf(order), oppositeBook.valueOf(top)) match {

      case (MarketPrice, LimitPrice(limit)) => trade(limit)

      case (LimitPrice(limit), LimitPrice(oppositeLimit)) => {
        if (crosses(limit, oppositeLimit)) trade(oppositeLimit)
        else None
      }

      case (LimitPrice(limit), MarketPrice) => trade(oppositeBook.bestLimit match {
        case Some(bestLimit) => if (crosses(limit, bestLimit)) bestLimit else limit
        case None => limit
      })

      case (MarketPrice, MarketPrice) => trade(oppositeBook.bestLimit match {
        case Some(limit) => limit
        case None => _referencePrice.getOrElse(throw new IllegalStateException("Can't execute a trade with two market orders without best limit or reference price"))
      })

      case (UndefinedPrice, _) | (_, UndefinedPrice) => None
    }
  }
}
