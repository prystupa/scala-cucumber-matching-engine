package com.prystupa.matching

import collection.mutable
import com.prystupa.matching.OrderList.Entry

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/8/13
 * Time: 8:21 PM
 */

object OrderList {

  sealed trait Entry {
    def order: Order

    def update(order: Order)

    def remove()
  }
}

class OrderList(first: Order) {

  private val beforeHead = mutable.DoubleLinkedList[Order](null, first)

  private def head = beforeHead.next

  private var last = head

  def append(order: Order): Entry = {
    val l = mutable.DoubleLinkedList(order)
    last append l
    last = l
    new LinkedListEntry(l)
  }

  def headEntry: Entry = new LinkedListEntry(head)

  def isEmpty = head.isEmpty

  def toTraversable = head

  private class LinkedListEntry(linkedList: mutable.DoubleLinkedList[Order]) extends Entry {
    def order: Order = linkedList.elem

    def remove() {
      linkedList.remove()
    }

    def update(order: Order) {
      linkedList.elem = order
    }
  }

}
