Feature: Core matching logic for market orders

  Scenario: Matching a large Buy market order against multiple limit orders
  The order is large enough to fill the entire opposite book
  The remainder of the market order is expected to rest in its book
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | 10.7  |
      | B      | Buy  | 200 | 10.6  |
      | C      | Buy  | 300 | 10.5  |
      | D      | Sell | 650 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | D              | 100 | 10.7  |
      | B             | D              | 200 | 10.6  |
      | C             | D              | 300 | 10.5  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      |        |     |       | MO    | 50  | D      |

  Scenario: Matching a small Buy market order against limit orders
  The order is small enough so it fills entirely
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | 10.7  |
      | B      | Buy  | 200 | 10.6  |
      | C      | Buy  | 300 | 10.5  |
      | D      | Sell | 150 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | D              | 100 | 10.7  |
      | B             | D              | 50  | 10.6  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 150 | 10.6  |       |     |        |
      | C      | 300 | 10.5  |       |     |        |

  Scenario: Matching a large Sell market order against multiple limit orders
  The order is large enough to fill the entire opposite book
  The remainder of the market order is expected to rest in its book
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Sell | 100 | 10.5  |
      | B      | Sell | 200 | 10.6  |
      | C      | Sell | 300 | 10.7  |
      | D      | Buy  | 650 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | D             | A              | 100 | 10.5  |
      | D             | B              | 200 | 10.6  |
      | D             | C              | 300 | 10.7  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | D      | 50  | MO    |       |     |        |

  Scenario: Matching a small Sell market order against limit orders
  The order is small enough so it fills entirely
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Sell | 100 | 10.5  |
      | B      | Sell | 200 | 10.6  |
      | C      | Sell | 300 | 10.7  |
      | D      | Buy  | 150 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | D             | A              | 100 | 10.5  |
      | D             | B              | 50  | 10.6  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      |        |     |       | 10.6  | 150 | B      |
      |        |     |       | 10.7  | 300 | C      |


#
# Matching incoming limit orders against outstanding market orders
#

  Scenario: Matching incoming Buy limit order against a single outstanding Sell market order
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Sell | 100 | MO    |
      | B      | Buy  | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | B             | A              | 100 | 10.5  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 20  | 10.5  |       |     |        |

  Scenario: Matching incoming Sell limit order against a single outstanding Buy market order
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | MO    |
      | B      | Sell | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | B              | 100 | 10.5  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      |        |     |       | 10.5  | 20  | B      |

  Scenario: Matching incoming Buy limit order against Sell market order while another NON-CROSSING Sell limit order is outstanding
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Sell | 100 | MO    |
      | B      | Sell | 100 | 10.6  |
      | C      | Buy  | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | C             | A              | 100 | 10.5  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | C      | 20  | 10.5  | 10.6  | 100 | B      |

  Scenario: Matching incoming Sell limit order against Buy market order while another NON-CROSSING Buy limit order is outstanding
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | MO    |
      | B      | Buy  | 100 | 10.4  |
      | C      | Sell | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | C              | 100 | 10.5  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 100 | 10.4  | 10.5  | 20  | C      |

  Scenario: Matching incoming Buy limit order against Sell market order while another CROSSING Sell limit order is outstanding
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Sell | 100 | MO    |
      | B      | Sell | 100 | 10.4  |
      | C      | Buy  | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | C             | A              | 100 | 10.4  |
      | C             | B              | 20  | 10.4  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      |        |     |       | 10.4  | 80  | B      |

  Scenario: Matching incoming Sell limit order against Buy market order while another CROSSING Buy limit order is outstanding
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | MO    |
      | B      | Buy  | 100 | 10.6  |
      | C      | Sell | 120 | 10.5  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | C              | 100 | 10.6  |
      | B             | C              | 20  | 10.6  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 80  | 10.6  |       |     |        |
