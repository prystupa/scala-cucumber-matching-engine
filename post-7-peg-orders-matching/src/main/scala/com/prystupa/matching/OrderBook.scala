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

  private case class OrdersAtLimit(limit: Double, orders: FastList[Order])

  private case class OrderLocation(list: FastList.Entry[OrdersAtLimit], entry: FastList.Entry[Order])

  private val marketBook: FastList[Order] = FastList()
  private val limitBook = FastList[OrdersAtLimit]()
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse
  private var pegs: FastList[OrderLocation] = FastList()

  def add(order: Order) {

    order match {
      case _: MarketOrder => marketBook.append(order)
      case LimitOrder(_, _, _, limit) => addLimit(limit, order)
      case _: PegOrder => addPeg(order, isNewOrder = true)
    }
  }

  def valueOf(order: Order): PriceLevel = order match {
    case _: MarketOrder => MarketPrice
    case LimitOrder(_, _, _, limit) => LimitPrice(limit)
    case _: PegOrder => bestLimit.map(LimitPrice(_)).getOrElse(UndefinedPrice)
  }

  def top: Option[Order] = marketBook.headOption orElse limitBook.headOption.map(_.orders.head)

  def bestLimit: Option[Double] = limitBook.headOption.map(_.limit)

  def modify(worker: ModifyOperations => Unit) {

    val book = this
    val oldBestLimit = bestLimit

    worker(new ModifyOperations {

      def top = book.top

      def decreaseTopBy(qty: Double) {
        def decrease(list: FastList[Order]) {
          val top = list.head
          if (qty == top.qty) list.removeTop()
          else list.updateTop(top.withQty(top.qty - qty))
        }

        marketBook.headOption match {
          case Some(_) => decrease(marketBook)
          case None => limitBook.headOption match {
            case Some(OrdersAtLimit(_, orders)) =>
              decrease(orders)
              if (orders.isEmpty) limitBook.removeTop()
            case None => throw new IllegalStateException("No top order in the book")
          }
        }
      }
    })

    if (bestLimitChanged(oldBestLimit)) updatePegs()
  }

  def orders(): List[Order] = marketBook.toList ++ limitBook.flatMap(_.orders)

  private def bestLimitChanged(old: Option[Double]): Boolean = {
    limitBook.headOption.withFilter(_.orders.exists({
      case _: LimitOrder => true
      case _ => false
    })).map(_.limit) != old
  }

  private def addPeg(order: Order, isNewOrder: Boolean) {
    bestLimit match {
      case Some(limit) =>
        val pegOrder = insertLimit(limit, order)
        pegs.append(pegOrder)
      case None => publish(if (isNewOrder) RejectedOrder(order) else CancelledOrder(order))
    }
  }

  private def addLimit(level: Double, order: Order) {

    val oldBestLimit = bestLimit
    insertLimit(level, order)
    if (oldBestLimit != bestLimit) updatePegs()
  }

  private def insertLimit(level: Double, order: Order): OrderLocation = {

    val entry = limitBook.getOrInsertAt(
      levelOrders => priceOrdering.compare(level, levelOrders.limit),
      OrdersAtLimit(level, FastList()))
    val OrdersAtLimit(_, orders) = entry.value

    OrderLocation(entry, orders.append(order))
  }

  private def removeLimit(orderLocation: OrderLocation) {

    val entry = orderLocation.entry
    val list = orderLocation.list
    val orders = list.value.orders
    entry.remove()
    if (orders.isEmpty) list.remove()
  }

  private def updatePegs() {

    val pegsToResubmit = pegs
    pegs = FastList()
    pegsToResubmit.foreach(peg => removeLimit(peg))
    pegsToResubmit.foreach(peg => addPeg(peg.entry.value, isNewOrder = false))
  }
}
