package com.prystupa.matching

import collection.mutable


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(val side: Side) {

  private val bookOrders: mutable.LinkedList[Order] = mutable.LinkedList.empty

  def add(order: LimitOrder) {

    insert(order, bookOrders)
  }

  def orders: List[Order] = bookOrders.toList

  private def compareOrders(order: LimitOrder, bookOrder: Order): Int = {
    bookOrder match {
      case LimitOrder(_, _, _, limit) => side match {
        case Buy => order.limit.compare(limit)
        case Sell => limit.compare(order.limit)
      }
    }
  }

  private def insert(order: LimitOrder, orders: mutable.LinkedList[Order]) {

    if (orders.isEmpty) {
      orders.elem = order
      orders.next = mutable.LinkedList.empty
    } else {
      val bookOrder = orders.elem
      if (compareOrders(order, bookOrder) > 0) {
        orders.next = new mutable.LinkedList(orders.elem, orders.next)
        orders.elem = order
      }
      else {
        insert(order, orders.next)
      }
    }
  }
}
