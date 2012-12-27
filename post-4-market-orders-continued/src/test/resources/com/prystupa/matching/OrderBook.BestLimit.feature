Feature: Maintaining best limit in the order book

  Scenario Outline: Various life cycles of the order book best limit
  # When there are no orders in the book, the best limit is not defined
    Then the best limit for "<Side>" order book is "None"
  # If a market order enters the book, the best limit is still undefined
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | A      | 100 | MO    |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | MO    |
    And the best limit for "<Side>" order book is "None"
  # If a first limit order enters the book it becomes best limit
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price         |
      | B      | 100 | <First Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price         |
      | A      | 100 | MO            |
      | B      | 100 | <First Limit> |
    And the best limit for "<Side>" order book is "<First Limit>"
  # When a second, more conservative limit order enters the book, the best limit does not change
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price                |
      | C      | 100 | <Conservative Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                |
      | A      | 100 | MO                   |
      | B      | 100 | <First Limit>        |
      | C      | 100 | <Conservative Limit> |
    And the best limit for "<Side>" order book is "<First Limit>"
  # When a third, more aggressive limit order enters the book, it becomes the new best limit
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price              |
      | D      | 100 | <Aggressive Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                |
      | A      | 100 | MO                   |
      | D      | 100 | <Aggressive Limit>   |
      | B      | 100 | <First Limit>        |
      | C      | 100 | <Conservative Limit> |
    And the best limit for "<Side>" order book is "<Aggressive Limit>"
  # When the top MO is removed (i.e. because it crossed) the best limit should not change
    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                |
      | D      | 100 | <Aggressive Limit>   |
      | B      | 100 | <First Limit>        |
      | C      | 100 | <Conservative Limit> |
    And the best limit for "<Side>" order book is "<Aggressive Limit>"
  # When the top aggressive order is removed (i.e. because it crossed) the best limit should become our first order
    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                |
      | B      | 100 | <First Limit>        |
      | C      | 100 | <Conservative Limit> |
    And the best limit for "<Side>" order book is "<First Limit>"
  # When the last two orders are removed (i.e. because they crossed) the best limit should become "None"
    When the top order goes away from the "<Side>" book
    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
    And the best limit for "<Side>" order book is "None"
  Examples:
    | Side | First Limit | Conservative Limit | Aggressive Limit |
    | Buy  | 10.5        | 10.4               | 10.6             |
    | Sell | 10.5        | 10.6               | 10.4             |
