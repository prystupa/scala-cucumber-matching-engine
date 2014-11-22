package com.prystupa.matching.step_definitions

import cucumber.api.DataTable
import com.prystupa.matching._
import org.scalatest.Matchers._

import scala.collection.JavaConversions._
import cucumber.api.java.en.{When, Then, Given}

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 3:22 PM
 */

class OrderBookSteps {

  val orderTypes = OrderType.all(buyBook, sellBook)
  val buyBook: OrderBook = new OrderBook(Buy, orderTypes)
  val sellBook: OrderBook = new OrderBook(Sell, orderTypes)

  var actualRejected = Vector.empty[Order]
  var actualCancelled = Vector.empty[Order]

  events {
    case RejectedOrder(order) => actualRejected :+= order
    case CancelledOrder(order) => actualCancelled :+= order
  }

  @Given("^the following orders are added to the \"([^\"]*)\" book:$")
  def the_following_orders_are_added_to_the_book(sideString: String, orderTable: DataTable) {

    val (side, book) = getBook(sideString)
    val orders = parseOrders(orderTable, side)

    orders.foreach(book.add)
  }

  @Then("^the \"([^\"]*)\" order book looks like:$")
  def the_order_book_looks_like(sideString: String, bookTable: DataTable) {

    val (_, book) = getBook(sideString)
    val expectedBook = bookTable.asList[BookRow](classOf[BookRow]).toList
    val actualBook = book.orders().map(o => BookRow(o.broker, o.qty, orderTypes(o).bookDisplay))

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
  def the_following_orders_are_rejected(sideString: String, orderTable: DataTable) {

    val (side, _) = getBook(sideString)
    val expected = parseOrders(orderTable, side)

    actualRejected should equal(expected)
    actualRejected = Vector.empty
  }

  @Then("^the following \"([^\"]*)\" orders are cancelled:$")
  def the_following_orders_are_cancelled(sideString: String, orderTable: DataTable) {

    val (side, _) = getBook(sideString)
    val expected = parseOrders(orderTable, side)

    actualCancelled should equal(expected)
    actualCancelled = Vector.empty
  }


  private def getBook(side: String) = side match {
    case "Buy" => (Buy, buyBook)
    case "Sell" => (Sell, sellBook)
  }

  private def parseOrders(orderTable: DataTable, side: Side): Vector[Order] = {
    orderTable.asList[OrderRow](classOf[OrderRow]).toVector.map(
      r => r.price match {
        case "MO" => MarketOrder(r.broker, side, r.qty)
        case "Peg" => PegOrder(r.broker, side, r.qty)
        case _ => LimitOrder(r.broker, side, r.qty, r.price.toDouble)
      })
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
