package com.prystupa.matching

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/10/13
 * Time: 7:51 AM
 */

@RunWith(classOf[JUnitRunner])
class FastListTest extends FunSuite {

  test("append, update, and remove a single element") {
    val target = FastList[Int]()
    val first = target append 1

    first.value should equal(1)
    target.toList should equal(List(1))

    first.update(11)
    target.toList should equal(List(11))

    first.remove()
    target.toList should equal(Nil)

  }

  test("append, update, and remove two elements") {
    val target = FastList[Int]()
    val first = target append 1
    val second = target append 2

    first.value should equal(1)
    second.value should equal(2)
    target.toList should equal(List(1, 2))

    first.update(11)
    second.update(22)
    target.toList should equal(List(11, 22))

    first.remove()
    second.value should equal(22)
    target.toList should equal(List(22))

    second.remove()
    target.toList should equal(Nil)
  }

  test("upsert a single element") {
    val target = FastList[Int]()

    val upserted = target.getOrInsertAt(null, 1)
    upserted.value should equal(1)
    target.toList should equal(List(1))
  }

  test("upsert before the first element") {
    val target = FastList[Int](2)

    val upserted = target.getOrInsertAt(1.compareTo(_), 1)
    upserted.value should equal(1)
    target.toList should equal(List(1, 2))

    target.removeTop()
    target.toList should equal(List(2))

    target.removeTop()
    target.toList should equal(Nil)
  }

  test("upsert between two entries") {
    val target = FastList[Int](1, 3)

    val upserted = target.getOrInsertAt(2.compareTo(_), 2)
    upserted.value should equal(2)
    target.toList should equal(List(1, 2, 3))

    target.removeTop()
    target.toList should equal(List(2, 3))

    target.removeTop()
    target.toList should equal(List(3))

    target.removeTop()
    target.toList should equal(Nil)
  }

  test("upsert after the last entry") {
    val target = FastList[Int](1, 2)

    val upserted = target.getOrInsertAt(3.compareTo(_), 3)
    upserted.value should equal(3)
    target.toList should equal(List(1, 2, 3))

    target.removeTop()
    target.toList should equal(List(2, 3))

    target.removeTop()
    target.toList should equal(List(3))

    target.removeTop()
    target.toList should equal(Nil)
  }

  test("upsert first and then upsert after first") {
    val target = FastList[Int]()

    val first = target.getOrInsertAt(1.compareTo(_), 1)
    val second = target.getOrInsertAt(2.compareTo(_), 2)
    first.value should equal(1)
    second.value should equal(2)
    target.toList should equal(List(1, 2))

    target.removeTop()
    target.toList should equal(List(2))

    target.removeTop()
    target.toList should equal(Nil)
  }

  test("merging with existing value") {
    val target = FastList[Int](1, 2, 3)

    val retrieved = target.getOrInsertAt(2.compareTo(_), 200)
    retrieved.value should equal(2)
    target.toList should equal(List(1, 2, 3))

    target.removeTop()
    target.toList should equal(List(2, 3))

    target.removeTop()
    target.toList should equal(List(3))

    target.removeTop()
    target.toList should equal(Nil)
  }

  test("append after removing the only element") {
    val target = FastList[Int]()

    val first = target.append(1)
    first.remove()

    target.append(2)

    target.toList should equal(List(2))
  }

  test("append after removing the last element") {
    val target = FastList[Int](1)

    val second = target.append(2)
    second.remove()

    target.append(3)

    target.toList should equal(List(1, 3))
  }
}
