package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(side: Side, orderTypes: (Order => OrderType)) {

  private var limitBook: List[(Double, List[Order])] = Nil
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse

  def add(order: Order) {

    orderTypes(order).price match {

      case LimitPrice(limit) => addLimit(limit, order)
    }
  }

  def orders(): List[Order] = limitBook.flatMap({
    case (_, orders) => orders
  })

  private def addLimit(limit: Double, order: Order) {
    def insert(list: List[(Double, List[Order])]): List[(Double, List[Order])] = list match {
      case Nil => List((limit, List(order)))
      case (head@(bookLevel, orders)) :: tail => priceOrdering.compare(limit, bookLevel) match {
        case 0 => (bookLevel, orders :+ order) :: tail
        case n if n < 0 => (limit, List(order)) :: list
        case _ => head :: insert(tail)
      }
    }

    limitBook = insert(limitBook)
  }
}
