package com.prystupa.matching.step_definitions

import cucumber.api.DataTable
import com.prystupa.matching._
import org.scalatest.matchers.ShouldMatchers

import scala.collection.JavaConversions._
import cucumber.api.java.en.{When, Then, Given}

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 3:22 PM
 */

class OrderBookSteps extends ShouldMatchers {

  val buyBook = new OrderBook(Buy)
  val sellBook = new OrderBook(Sell)


  @Given("^the following orders are added to the \"([^\"]*)\" book:$")
  def the_following_orders_are_added_to_the_book(side: String, orderTable: DataTable) {

    val book = getBook(side)
    val orders = orderTable.asList[OrderRow](classOf[OrderRow]).toList.map(
      r => LimitOrder(r.broker, book.side, r.qty, r.price))

    orders.foreach(book.add)
  }

  @Then("^the \"([^\"]*)\" order book looks like:$")
  def the_order_book_looks_like(side: String, bookTable: DataTable) {

    val book = getBook(side)
    val expectedBook = bookTable.asList[BookRow](classOf[BookRow]).toList
    val actualBook = book.orders.map(o => BookRow(o.broker, o.qty, o.bookDisplay))

    actualBook should equal(expectedBook)
  }

  @When("^the top order of the \"([^\"]*)\" book is filled by \"([^\"]*)\"$")
  def the_top_order_of_the_book_is_filled_by(side: String, qty: Double) {

    val book = getBook(side)
    book.decreaseTopBy(qty)
  }


  def getBook(side: String): OrderBook = side match {
    case "Buy" => buyBook
    case "Sell" => sellBook
  }

  case class OrderRow(broker: String, qty: Double, price: Double)

  case class BookRow(broker: String, qty: Double, price: String)

}
