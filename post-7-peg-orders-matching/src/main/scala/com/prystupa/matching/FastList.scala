package com.prystupa.matching

import collection.mutable
import com.prystupa.matching.FastList.Entry

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/9/13
 * Time: 9:15 PM
 */

object FastList {

  def apply[A](elems: A*): FastList[A] = new FastList(elems: _*)

  sealed trait Entry[A] {

    def value: A

    def remove()

    def update(elem: A)
  }

}

class FastList[A] private(seed: mutable.DoubleLinkedList[A]) extends Iterable[A] {

  private var lastEntry = first

  private def first = seed.next

  def this(elems: A*) = this(mutable.DoubleLinkedList[A]((mutable.DoubleLinkedList[A]().elem +: elems): _*))

  def append(elem: A): Entry[A] = {
    val prevLast = (if (isEmpty) seed else lastEntry)
    lastEntry = mutable.DoubleLinkedList(elem)
    prevLast append (lastEntry)
    new ListEntry(lastEntry)
  }

  def getOrInsertAt(positionSelector: A => Int, insert: => A): Entry[A] = {

    var iter, node = first
    var lastCompare = 1
    while (!iter.isEmpty && lastCompare > 0) {
      node = iter
      lastCompare = positionSelector(iter.elem)
      iter = iter.next
    }

    val upserted =
      if (lastCompare > 0) {
        val inserted = mutable.DoubleLinkedList(insert)
        (if (node.isEmpty) node.prev else node).insert(inserted)
        inserted
      }
      else if (lastCompare < 0) {
        val inserted = mutable.DoubleLinkedList(insert)
        node.prev.insert(inserted)
        inserted
      }
      else node

    new ListEntry(upserted)
  }

  def iterator: Iterator[A] = first.iterator

  def removeTop() {
    first.remove()
  }

  def updateTop(elem: A) {
    first.elem = elem
  }

  private case class ListEntry(node: mutable.DoubleLinkedList[A]) extends Entry[A] {

    def value: A = node.elem

    def remove() {
      node.remove()
    }

    def update(elem: A) {
      node.elem = elem
    }
  }

}
