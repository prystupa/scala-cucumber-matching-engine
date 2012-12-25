package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:14 PM
 */

class MatchingEngine(buy: OrderBook, sell: OrderBook) {

  def acceptOrder(order: LimitOrder): List[Trade] = {

    val (book, counterBook) = getBooks(order.side)
    val (unfilledOrder, trades) = tryMatch(order, counterBook, Nil)
    unfilledOrder.foreach(book.add(_))

    trades.reverse
  }


  private def getBooks(side: Side): (OrderBook, OrderBook) = side match {
    case Buy => (buy, sell)
    case Sell => (sell, buy)
  }

  private def tryMatch(order: LimitOrder, counterBook: OrderBook, trades: List[Trade]): (Option[LimitOrder], List[Trade]) = {

    if (order.qty == 0) (None, trades)
    else if (counterBook.orders.isEmpty) (Some(order), trades)
    else tryMatchWithTop(order, counterBook.orders.head) match {
      case None => (Some(order), trades)
      case Some(trade) => {
        counterBook.decreaseTopBy(trade.qty)
        tryMatch(order.decreasedBy(trade.qty), counterBook, trade :: trades)
      }
    }
  }

  private def tryMatchWithTop(order: LimitOrder, top: Order): Option[Trade] = top match {

    case topLimitOrder: LimitOrder => {
      val (buy, sell) = if (order.side == Buy) (order, topLimitOrder) else (topLimitOrder, order)

      if (buy.limit >= sell.limit) Some(Trade(buy.broker, sell.broker, topLimitOrder.limit, math.min(buy.qty, sell.qty)))
      else None
    }
  }
}
