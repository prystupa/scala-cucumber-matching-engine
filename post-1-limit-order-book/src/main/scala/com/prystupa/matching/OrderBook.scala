package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(val side: Side) {

  private var book: List[Order] = Nil

  def add(order: Order) {

    def insert(orders: List[Order]): List[Order] = orders match {
      case Nil => List(order)
      case bookOrder :: tail =>
        if (compareOrders(order, bookOrder) > 0) order :: bookOrder :: tail
        else bookOrder :: insert(tail)
    }

    book = insert(book)
  }

  def orders: List[Order] = book


  private def compareOrders(order: Order, bookOrder: Order): Int = (order, bookOrder) match {
    case (LimitOrder(_, _, _, limit), LimitOrder(_, _, _, bookLimit)) => side match {
      case Buy => limit.compare(bookLimit)
      case Sell => bookLimit.compare(limit)
    }
  }
}
