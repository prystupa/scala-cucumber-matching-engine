package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(val side: Side) {

  private var book: List[Order] = Nil

  def add(order: LimitOrder) {

    def insert(orders: List[Order]): List[Order] = orders match {
      case Nil => List(order)
      case bookOrder :: tail =>
        if (compareOrders(order, bookOrder) > 0) order :: bookOrder :: tail
        else bookOrder :: insert(tail)
    }

    book = insert(book)
  }

  def orders: List[Order] = book

  def decreaseTopBy(qty: Double) {
    book = book match {
      case head :: tail => if (qty == head.qty) tail else head.decreasedBy(qty) :: tail
      case Nil => throw new IllegalStateException("Can't decrease top order in empty book")
    }
  }

  private def compareOrders(order: LimitOrder, bookOrder: Order): Int = bookOrder match {
    case LimitOrder(_, _, _, limit) => side match {
      case Buy => order.limit.compare(limit)
      case Sell => limit.compare(order.limit)
    }
  }
}
