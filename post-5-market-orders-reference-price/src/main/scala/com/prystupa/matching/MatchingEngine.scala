package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:14 PM
 */

class MatchingEngine(buy: OrderBook, sell: OrderBook, orderTypes: (Order => OrderType)) {

  private var _referencePrice: Option[Double] = None

  def referencePrice = _referencePrice.get

  def referencePrice_=(price: Double) {
    _referencePrice = Some(price)
  }

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
