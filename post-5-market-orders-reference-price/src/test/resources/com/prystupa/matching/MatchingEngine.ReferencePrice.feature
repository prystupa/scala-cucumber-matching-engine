Feature: Maintaining reference price as trades occur

  Scenario: Updating reference price as it is set at the opening and trades occur
    Given the reference price is set to "10"

    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | 11    |
      | B      | Sell | 100 | 11    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | B              | 100 | 11    |
    And the reference price is reported as "11"

    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | 12    |
      | B      | Sell | 100 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | B              | 100 | 12    |
    And the reference price is reported as "12"

    When the following orders are submitted in this order:
      | Broker | Side | Qty | Price |
      | A      | Buy  | 100 | MO    |
      | B      | Sell | 100 | MO    |
    Then the following trades are generated:
      | Buying broker | Selling broker | Qty | Price |
      | A             | B              | 100 | 12    |
    And the reference price is reported as "12"
