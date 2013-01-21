Feature: Matching pegged orders

  Background:
    Given market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |

  Scenario Outline: Submitting pegged order to an empty book or the book with only market orders on its side
    When the following orders are submitted in this order:
      | Broker | Side   | Qty | Price |
      | A      | <Side> | 100 | Peg   |
    Then the following orders are rejected:
      | Broker | Side   | Qty | Price |
      | A      | <Side> | 100 | Peg   |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |

    When the following orders are submitted in this order:
      | Broker | Side   | Qty | Price |
      | A      | <Side> | 100 | MO    |
      | B      | <Side> | 100 | Peg   |
    Then the following orders are rejected:
      | Broker | Side   | Qty | Price |
      | B      | <Side> | 100 | Peg   |

  Examples:
    | Side |
    | Buy  |
    | Sell |


  Scenario Outline: Submitting pegged order to empty book is rejected even if there is a matching order in the opposite book
    When the following orders are submitted in this order:
      | Broker | Side            | Qty | Price |
      | A      | <Opposite Side> | 100 | MO    |
      | B      | <Side>          | 100 | Peg   |
    Then the following orders are rejected:
      | Broker | Side   | Qty | Price |
      | B      | <Side> | 100 | Peg   |
  Examples:
    | Side | Opposite Side |
    | Buy  | Sell          |
    | Sell | Buy           |
