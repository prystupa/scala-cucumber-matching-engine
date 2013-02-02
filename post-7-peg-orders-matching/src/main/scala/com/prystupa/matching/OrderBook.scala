package com.prystupa.matching

import com.prystupa.matching.OrderBook.ModifyOperations
import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

object OrderBook {

  sealed trait ModifyOperations {

    def top: Option[Order]

    def decreaseTopBy(qty: Double)
  }

}

class OrderBook(side: Side) extends mutable.Publisher[OrderBookEvent] {

  private case class OrdersAtLimit(limit: Double, orders: FastList[Order], pegs: FastList[OrderLocation])

  private case class OrderLocation(list: FastList.Entry[OrdersAtLimit], entry: FastList.Entry[Order])

  private val marketBook: FastList[Order] = FastList()
  private val limitBook = FastList[OrdersAtLimit]()
  private val priceOrdering = if (side == Sell) Ordering.ordered[Double] else Ordering.ordered[Double].reverse

  def add(order: Order) {

    order match {
      case _: MarketOrder => marketBook.append(order)
      case LimitOrder(_, _, _, limit) => addLimit(limit, order)
      case peg: PegOrder => addPeg(peg, isNewOrder = true)
    }
  }

  def valueOf(order: Order): PriceLevel = order match {
    case _: MarketOrder => MarketPrice
    case LimitOrder(_, _, _, limit) => LimitPrice(limit)
    case PegOrder(_, _, _, limit) => bestLimit.map(bl => {
      LimitPrice(limit.map(l => priceOrdering.max(l, bl)).getOrElse(bl))
    }).getOrElse(UndefinedPrice)
  }

  def top: Option[Order] = marketBook.headOption orElse limitBook.headOption.map(_.orders.head)

  def bestLimit: Option[Double] = limitBook.headOption.map(_.limit)

  def modify(worker: ModifyOperations => Unit) {

    val oldBestLimit = bestLimit

    worker(new InvariantBookModifications)

    val newBestLimit = searchBestLimit()
    if (newBestLimit != oldBestLimit) updatePegsOnBestLimitWorsened(newBestLimit)
  }

  private class InvariantBookModifications extends ModifyOperations {
    def top: Option[Order] = OrderBook.this.top

    def decreaseTopBy(qty: Double) {
      def decrease(list: FastList[Order]) {
        val top = list.head
        if (qty == top.qty) list.removeTop()
        else list.updateTop(top.withQty(top.qty - qty))
      }

      marketBook.headOption match {
        case Some(_) => decrease(marketBook)
        case None => limitBook.headOption match {
          case Some(OrdersAtLimit(_, orders, _)) =>
            decrease(orders)
            if (orders.isEmpty) limitBook.removeTop()
          case None => throw new IllegalStateException("No top order in the book")
        }
      }
    }
  }

  def orders(): List[Order] = {
    marketBook.toList ++ limitBook.flatMap(_.orders)
  }


  private def addPeg(order: PegOrder, isNewOrder: Boolean) {
    bestLimit match {
      case Some(bl) =>
        val location = insertLimit(order.limit.map(priceOrdering.max(_, bl)).getOrElse(bl), order)
        location.list.value.pegs.append(location)
      case None => publish(if (isNewOrder) RejectedOrder(order) else CancelledOrder(order))
    }
  }

  private def addLimit(level: Double, order: Order) {

    val oldBestLimit = bestLimit
    val oldTopLevel = limitBook.headOption
    insertLimit(level, order)

    oldTopLevel.foreach(level => {
      if (oldBestLimit != bestLimit) updatePegsOnBestLimitImproved(level)
    })
  }

  private def insertLimit(level: Double, order: Order): OrderLocation = {

    val entry = limitBook.getOrInsertAt(
      levelOrders => priceOrdering.compare(level, levelOrders.limit),
      OrdersAtLimit(level, FastList(), FastList()))
    val OrdersAtLimit(_, orders, _) = entry.value

    OrderLocation(entry, orders.append(order))
  }

  private def removeLimit(orderLocation: OrderLocation) {

    val entry = orderLocation.entry
    val list = orderLocation.list
    val orders = list.value.orders
    entry.remove()
    if (orders.isEmpty) list.remove()
  }

  private def searchBestLimit(): Option[Double] = {
    def search(book: Iterable[OrdersAtLimit]): Option[Double] = {
      if (book.isEmpty) None
      else book.head.orders.collectFirst({
        case LimitOrder(_, _, _, limit) => limit
      }) orElse search(book.tail)
    }

    search(limitBook)
  }

  private def updatePegsOnBestLimitImproved(level: OrdersAtLimit) {

    val pegsToResubmit = FastList[OrderLocation]()
    level.pegs.removeInto(pegsToResubmit, l => l.entry.value match {
      case PegOrder(_, _, _, limit) => limit != Some(level.limit)
      case _ => false
    })

    resubmitPegs(pegsToResubmit)
  }

  private def updatePegsOnBestLimitWorsened(lastLimit: Option[Double]) {
    def find(levels: Iterable[OrdersAtLimit], results: FastList[OrderLocation]): Any = {
      levels.headOption.foreach(level => {
        if (lastLimit.map(ll => priceOrdering.gt(ll, level.limit)).getOrElse(true)) {
          level.pegs.removeInto(results, l => l.entry.value match {
            case PegOrder(_, _, _, limit) => limit.map(_ => true).getOrElse(true)
            case _ => false
          })
          find(levels.tail, results)
        }
      })
    }

    val pegsToResubmit = FastList[OrderLocation]()
    find(limitBook, pegsToResubmit)

    resubmitPegs(pegsToResubmit)
  }

  private def resubmitPegs(pegs: FastList[OrderLocation]) {
    pegs.foreach(removeLimit)
    pegs.foreach(_.entry.value match {
      case peg: PegOrder => addPeg(peg, isNewOrder = false)
      case _ => throw new IllegalStateException()
    })
  }
}
