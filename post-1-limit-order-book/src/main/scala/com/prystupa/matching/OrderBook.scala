package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(val side: Side) {

  private var limit: List[(Double, List[Order])] = Nil
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse

  def add(order: Order) {

    val level = priceLevel(order)

    def insert(list: List[(Double, List[Order])]): List[(Double, List[Order])] = list match {
      case Nil => List((level, List(order)))
      case (head@(bookLevel, orders)) :: tail => priceOrdering.compare(level, bookLevel) match {
        case 0 => (bookLevel, orders :+ order) :: tail
        case n if n < 0 => (level, List(order)) :: list
        case _ => head :: insert(tail)
      }
    }

    limit = insert(limit)
  }

  def orders(): List[Order] = limit.flatMap({
    case (_, orders) => orders
  })

  private def priceLevel(order: Order): Double = order match {
    case LimitOrder(_, _, _, priceLimit) => priceLimit
  }
}
