package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(val side: Side) {

  private var market: List[Order] = Nil
  private var book: List[(Double, List[Order])] = Nil
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse

  def add(order: Order) {

    priceLevel(order) match {
      case None => market = market :+ order
      case Some(level) => {
        def insert(list: List[(Double, List[Order])]): List[(Double, List[Order])] = list match {
          case Nil => List((level, List(order)))
          case (head@(bookLevel, orders)) :: tail => priceOrdering.compare(level, bookLevel) match {
            case 0 => (bookLevel, orders :+ order) :: tail
            case n if n < 0 => (level, List(order)) :: list
            case _ => head :: insert(tail)
          }
        }
        book = insert(book)
      }
    }
  }

  def top: Option[Order] = market match {
    case head :: _ => Some(head)
    case _ => book.headOption.map({
      case (_, orders) => orders.head
    })
  }

  def decreaseTopBy(qty: Double) {

    market match {
      case top :: tail => market = if (qty == top.qty) tail else top.decreasedBy(qty) :: tail
      case _ => book match {
        case ((level, orders) :: tail) => {
          val (top :: rest) = orders
          book = (qty == top.qty, rest.isEmpty) match {
            case (true, true) => tail
            case (true, false) => (level, rest) :: tail
            case _ => (level, top.decreasedBy(qty) :: rest) :: tail
          }
        }
        case Nil => throw new IllegalStateException()
      }
    }
  }

  def orders(): List[Order] = market ::: book.flatMap({
    case (_, orders) => orders
  })

  private def priceLevel(order: Order): Option[Double] = order match {
    case LimitOrder(_, _, _, limit) => Some(limit)
    case _: MarketOrder => None
  }
}
