Feature: Core matching logic for limit orders

  Background: Submit initial non-crossing orders to work with
    Given the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | 10.4  |
      | B      | Buy  | 200 | 10.3  |
      | C      | Sell | 100 | 10.7  |
      | D      | Sell | 200 | 10.8  |
    Then no trades are generated
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | A      | 100 | 10.4  | 10.7  | 100 | C      |
      | B      | 200 | 10.3  | 10.8  | 200 | D      |

  Scenario Outline: Matching a single Buy order against identical in quantity outstanding Sell order
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price             |
      | E      | Buy  | 100 | <Buy Order Limit> |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price                  |
      | E             | C              | 100 | <Expected Trade Price> |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | A      | 100 | 10.4  | 10.8  | 200 | D      |
      | B      | 200 | 10.3  |       |     |        |
  Examples:
    | Buy Order Limit | Expected Trade Price | Comments                                       |
    | 10.7            | 10.7                 | Exact same price as top of the Sell order book |
    | 10.8            | 10.7                 | Higher price then the top of the Sell book     |

  Scenario Outline: Matching a single Sell order against identical outstanding Buy order
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price              |
      | E      | Sell | 100 | <Sell Order Limit> |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price                  |
      | A             | E              | 100 | <Expected Trade Price> |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 200 | 10.3  | 10.7  | 100 | C      |
      |        |     |       | 10.8  | 200 | D      |
  Examples:
    | Sell Order Limit | Expected Trade Price | Comments                                          |
    | 10.4             | 10.4                 | Exact same price as the top of the Buy order book |
    | 10.3             | 10.4                 | Lower price than the top of the Buy book          |

  Scenario: Matching a Buy order large enough to clear the Sell book
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | E      | Buy  | 350 | 10.8  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | E             | C              | 100 | 10.7  |
      | E             | D              | 200 | 10.8  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | E      | 50  | 10.8  |       |     |        |
      | A      | 100 | 10.4  |       |     |        |
      | B      | 200 | 10.3  |       |     |        |

  Scenario: Matching a Sell order large enough to clear the Buy book
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | E      | Sell | 350 | 10.3  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | E              | 100 | 10.4  |
      | B             | E              | 200 | 10.3  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      |        |     |       | 10.3  | 50  | E      |
      |        |     |       | 10.7  | 100 | C      |
      |        |     |       | 10.8  | 200 | D      |

  Scenario: Matching a large Buy order partially
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | E      | Buy  | 350 | 10.7  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | E             | C              | 100 | 10.7  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | E      | 250 | 10.7  | 10.8  | 200 | D      |
      | A      | 100 | 10.4  |       |     |        |
      | B      | 200 | 10.3  |       |     |        |

  Scenario: Matching a large Sell order partially
    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | E      | Sell | 350 | 10.4  |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | E              | 100 | 10.4  |
    And market order book looks like:
      | Broker | Qty | Price | Price | Qty | Broker |
      | B      | 200 | 10.3  | 10.4  | 250 | E      |
      |        |     |       | 10.7  | 100 | C      |
      |        |     |       | 10.8  | 200 | D      |