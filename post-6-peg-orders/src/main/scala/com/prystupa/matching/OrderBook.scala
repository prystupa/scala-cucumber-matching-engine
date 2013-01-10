package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(side: Side, orderTypes: (Order => OrderType)) {

  private case class OrdersAtLimit(limit: Double, orders: FastList[Order])

  private val market: FastList[Order] = FastList()
  private val limit = FastList[OrdersAtLimit]()
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse
  private var pegs: FastList[FastList.Entry[Order]] = FastList()

  def add(order: Order) {

    orderTypes(order).price match {

      case MarketPrice => market.append(order)

      case LimitPrice(level) => {
        val oldBestLimit = bestLimit
        insertLimit(level, order)
        if (oldBestLimit != bestLimit) updatePegs()
      }

      case PegPrice(level) => {
        val pegOrder = insertLimit(level, order)
        pegs.append(pegOrder)
      }
    }
  }

  def top: Option[Order] = market.headOption orElse limit.headOption.map(_.orders.head)

  def bestLimit: Option[Double] = limit.headOption.map(_.limit)

  def decreaseTopBy(qty: Double) {
    def decrease(list: FastList[Order], top: Order) {
      if (qty == top.qty) list.removeTop()
      else list.updateTop(orderTypes(top).decreasedBy(qty))
    }

    market.headOption match {
      case Some(top) => decrease(market, top)
      case None => limit.headOption match {
        case Some(OrdersAtLimit(_, orders)) =>
          decrease(orders, orders.head)
          if (orders.isEmpty) limit.removeTop()
        case None => throw new IllegalStateException("No top order in the book")
      }
    }
  }

  def orders(): List[Order] = market.toList ++ limit.flatMap(_.orders)

  private def insertLimit(level: Double, order: Order): FastList.Entry[Order] = {

    val OrdersAtLimit(_, orders) = limit.getOrInsertAt(
      levelOrders => priceOrdering.compare(level, levelOrders.limit),
      OrdersAtLimit(level, FastList()))

    orders.append(order)
  }

  private def updatePegs() {
    val pegsToResubmit = pegs
    pegs = FastList()
    pegsToResubmit.foreach(peg => {
      peg.remove()
      add(peg.value)
    })
  }
}
