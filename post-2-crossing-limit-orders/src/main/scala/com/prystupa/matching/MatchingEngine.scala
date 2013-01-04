package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:14 PM
 */

class MatchingEngine(buy: OrderBook, sell: OrderBook, orderTypes: (Order => OrderType)) {

  def acceptOrder(order: Order): List[Trade] = {

    val (book, counterBook) = getBooks(order.side)
    val (unfilledOrder, trades) = tryMatch(order, counterBook, Nil)
    unfilledOrder.foreach(book.add(_))

    trades.reverse
  }


  private def getBooks(side: Side): (OrderBook, OrderBook) = side match {
    case Buy => (buy, sell)
    case Sell => (sell, buy)
  }

  private def tryMatch(order: Order, counterBook: OrderBook, trades: List[Trade]): (Option[Order], List[Trade]) = {

    if (order.qty == 0) (None, trades)
    else counterBook.top match {
      case None => (Some(order), trades)
      case Some(top) => tryMatchWithTop(order, top) match {
        case None => (Some(order), trades)
        case Some(trade) => {
          counterBook.decreaseTopBy(trade.qty)
          tryMatch(orderTypes(order).decreasedBy(trade.qty), counterBook, trade :: trades)
        }
      }
    }
  }

  private def tryMatchWithTop(order: Order, top: Order): Option[Trade] = top match {

    case topLimit: LimitOrder => {
      if (orderTypes(order).crossesAt(topLimit.limit)) {
        val (buy, sell) = if (order.side == Buy) (order, topLimit) else (topLimit, order)
        Some(Trade(buy.broker, sell.broker, topLimit.limit, math.min(buy.qty, sell.qty)))
      }
      else None
    }
  }
}
