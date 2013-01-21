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

class OrderBookSteps extends OrderStepUtils with ShouldMatchers {

  val buyBook: OrderBook = new OrderBook(Buy)
  val sellBook: OrderBook = new OrderBook(Sell)

  var actualRejected = Vector.empty[Order]
  var actualCancelled = Vector.empty[Order]

  events {
    case RejectedOrder(order) => actualRejected :+= order
    case CancelledOrder(order) => actualCancelled :+= order
  }

  @Given("^the following orders are added to the \"([^\"]*)\" book:$")
  def the_following_orders_are_added_to_the_book(side: String, orderTable: java.util.List[OrderRow]) {

    val (_, book) = getBook(side)
    val orders = orderTable.toVector.map(r => parseOrder(r.broker, side, r.qty, r.price))

    orders.foreach(book.add)
  }

  @Then("^the \"([^\"]*)\" order book looks like:$")
  def the_order_book_looks_like(sideString: String, bookTable: DataTable) {

    val (_, book) = getBook(sideString)
    val expectedBook = bookTable.asList[BookRow](classOf[BookRow]).toList
    val actualBook = book.orders().map(o => BookRow(o.broker, o.qty, bookDisplay(o)))

    actualBook should equal(expectedBook)
  }

  @When("^the top order of the \"([^\"]*)\" book is filled by \"([^\"]*)\"$")
  def the_top_order_of_the_book_is_filled_by(sideString: String, qty: Double) {

    val (_, book) = getBook(sideString)
    book.modify(_.decreaseTopBy(qty))
  }

  @Then("^the best limit for \"([^\"]*)\" order book is \"([^\"]*)\"$")
  def the_best_limit_for_order_book_is(sideString: String, expectedBestLimit: String) {

    val (_, book) = getBook(sideString)
    val actual = book.bestLimit
    val expected = expectedBestLimit match {
      case "None" => None
      case s => Some(s.toDouble)
    }

    actual should equal(expected)
  }

  @When("^the top order goes away from the \"([^\"]*)\" book$")
  def the_top_order_goes_away_from_the_book(sideString: String) {

    val (_, book) = getBook(sideString)
    book.modify(_.decreaseTopBy(book.orders().head.qty))
  }

  @Then("^the following \"([^\"]*)\" orders are rejected:$")
  def the_following_orders_are_rejected(side: String, orderTable: java.util.List[OrderRow]) {

    val expected = orderTable.toVector.map(r => parseOrder(r.broker, side, r.qty, r.price))

    actualRejected should equal(expected)
    actualRejected = Vector.empty
  }

  @Then("^the following \"([^\"]*)\" orders are cancelled:$")
  def the_following_orders_are_cancelled(side: String, orderTable: java.util.List[OrderRow]) {

    val expected = orderTable.toVector.map(r => parseOrder(r.broker, side, r.qty, r.price))

    actualCancelled should equal(expected)
    actualCancelled = Vector.empty
  }


  private def events(handler: PartialFunction[OrderBookEvent, Unit]) {
    List(buyBook, sellBook).foreach(book => book.subscribe(new book.Sub {
      def notify(pub: book.Pub, event: OrderBookEvent) {
        handler(event)
      }
    }))
  }

  private case class OrderRow(broker: String, qty: Double, price: String)

  private case class BookRow(broker: String, qty: Double, price: String)

}
