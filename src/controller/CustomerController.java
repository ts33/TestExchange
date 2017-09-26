package controller;

import model.Customer;
import model.Order;
import model.SaxosReceipt;
import model.OrderTuple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerController {
    OrderController orderController;
    SaxosController saxosController;
    HashMap<String, Customer> customerMap;

    public CustomerController(OrderController orderController, SaxosController saxosController){
        this.orderController = orderController;
        this.saxosController = saxosController;

        String csvFile = "src/resources/customer.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        customerMap = new HashMap<>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] customerOrders = line.split(cvsSplitBy);
                if(customerMap.get(customerOrders[0]) == null){
                    // if customer doesnt exist, create and add transactions and derivatives
                    Customer customer = new Customer(customerOrders[0]);
                    String identifierStr = customerOrders[1];
                    BigDecimal assetUnits = new BigDecimal(customerOrders[2]);
                    BigDecimal txUnits = new BigDecimal(customerOrders[3]);
                    BigDecimal value = orderController.getValue(identifierStr);
                    Order tx = new Order(txUnits, value, identifierStr);
                    Order asset = new Order(assetUnits, value, identifierStr);
                    customer.addTransactions(tx);
                    customer.addDerivatives(asset);
                    customerMap.put(customerOrders[0], customer);

                } else {
                    // if customer already exists, add new transactions and derivatives
                    Customer c = customerMap.get(customerOrders[0]);
                    String identifierStr = customerOrders[1];
                    BigDecimal assetUnits = new BigDecimal(customerOrders[2]);
                    BigDecimal txUnits = new BigDecimal(customerOrders[3]);
                    BigDecimal value = orderController.getValue(identifierStr);
                    Order tx = new Order(txUnits, value, identifierStr);
                    Order asset = new Order(assetUnits, value, identifierStr);
                    c.addTransactions(tx);
                    c.addDerivatives(asset);
                    customerMap.put(customerOrders[0], c);

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Customer> getCustomers(){
        return new ArrayList(customerMap.values());
    }

    public BigDecimal getCustomersTotalAum(){
        BigDecimal result = BigDecimal.ZERO;
        for (Customer x: customerMap.values()){
            result = result.add(x.calculateTotalAum());
        }
        return result;
    }

    public BigDecimal getCustomersTotalCash(){
        BigDecimal result = BigDecimal.ZERO;
        for (Customer x: customerMap.values()){
            result = result.add(x.getCash());
        }
        return result;
    }

    public BigDecimal getCustomersTotalDerivatives(){
        BigDecimal result = BigDecimal.ZERO;
        for (Customer x: customerMap.values()){
            result = result.add(x.calculateTotalDerivatives());
        }
        return result;
    }

    // no more pending transactions for each customer
    public boolean allCustomersSettled(){
        boolean result = true;
        for (Customer x: customerMap.values()){
            if (x.getTransactions().size() > 0){
                result = false;
            }
        }
        return result;
    }

    // no negative values for derivative units
    public boolean allCustomersPositiveDerivative(){
        boolean result = true;
        for (Customer x: customerMap.values()){
            List<Order> derivatives = x.getDerivatives();
            for (Order o: derivatives){
                // if units is negative
                if (o.getUnits().compareTo(BigDecimal.ZERO) == -1){
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * TODO: Complete this method to pass the JUnit test
     *
     * Scenario: All customers have a list of pending transactions (getTransactions()), a list of current holdings (getDerivatives()), amongst other things. All these are represented by simplistically as Order objects.
     * Objective: All pending transactions need to be settled by sending relevant orders to our brokerage, Saxos
     * Methodology: This is done by sending a List of Order objects to SaxosController by calling processOrder()
     *
     * Saxos only accepts integer units. No 20.33 units.
     * Negative units are to sell the orders
     * Positive units are to buy the orders
     * Saxos will charge a transaction fee of $5 for each Order in the List of Order objects
     * Saxos will return a List of SaxosReceipt objects
     *
     * You can use the completeTransaction() method in Customer which takes in an Order object.
     * THIS WILL GENERATED BY YOU.
     * The complete transaction method checks the identifier, as well as the number of units in the passed Order object
     * If the passed Order object's units is LESS THAN the pending order's units, the pending transaction will not be removed.
     * The pending transaction will deduct what was completed and keep the remainder of the units. e.g. if completed order has 10 units, and pending is 11. Pending will now have 1 unit.
     *
     * If the completed order is LARGER or EQUALS to the pending transaction, the pending transaction is considered completed, and it is removed.
     *
     * Following which, the completed order will be deducted from the current holding, and the gains from the sale will be placed in the customer's cash (getCash()).
     * So if the completed order is 10 units, the holding is 100 units; the remainder will be 90 units.
     *
     * Do not over or undersell by too large a margin. No customer should be left holding derivatives with negative units. e.g. holding has -10 units.
     *
     */
    public void settleAllCustomersTransactions() throws Exception {

        HashMap<String, List<OrderTuple>> ordersMap = new HashMap<>();
        HashMap<String, BigDecimal> ordersSumMap = new HashMap<>();

        // 1. grouping the orders by product at the top level, then customer and units
        for (Customer x : customerMap.values()) {
            List<Order> orders = x.getTransactions();

            for (Order o : orders) {
                if (ordersMap.get(o.getIdentifier()) == null) {
                    List<OrderTuple> orderTuples = new ArrayList<>();
                    OrderTuple tuple = new OrderTuple(x.getName(), o.getUnits());
                    orderTuples.add(tuple);
                    ordersMap.put(o.getIdentifier(), orderTuples);
                } else {
                    List<OrderTuple> orderTuples = ordersMap.get(o.getIdentifier());
                    OrderTuple tuple = new OrderTuple(x.getName(), o.getUnits());
                    orderTuples.add(tuple);
                    ordersMap.put(o.getIdentifier(), orderTuples);
                }
            }
        }

        // 2. getting the sum by each product
        for (String identifier : ordersMap.keySet()) {
            BigDecimal sumAllUnits = BigDecimal.ZERO;
            List<OrderTuple> tupleList = ordersMap.get(identifier);
            for (OrderTuple tup : tupleList) {
                sumAllUnits = sumAllUnits.add(tup.getUnits());
            }
            ordersSumMap.put(identifier, sumAllUnits);
        }


        // for each product
        for (String identifier : ordersMap.keySet()) {
            BigDecimal sumAllUnits = ordersSumMap.get(identifier);
            BigDecimal roundedSum = roundAbsDown(sumAllUnits);
            System.out.println("sending an order to saxos for product: "+identifier+
                    ", with total units "+sumAllUnits+" rounded down to "+roundedSum);

            // round down the sum and send include in the receipt
            Order saxosOrder = new Order(roundedSum, BigDecimal.ZERO, identifier); // value is not utilized
            SaxosReceipt receipt = saxosController.parseOrder(saxosOrder);

            // find the value of each unit from the receipt
            String saxosProductId = receipt.getIdentifier();
            BigDecimal saxosUnits = receipt.getTotalUnits();
            BigDecimal saxosValue = receipt.getTotalValue().divide(receipt.getTotalUnits().abs(), 2, BigDecimal.ROUND_HALF_UP);
            System.out.println("received receipt from saxos for product: "+saxosProductId+
                    ", with total units "+saxosUnits+" and unit value "+saxosValue);

            List<OrderTuple> tupleList = ordersMap.get(identifier);
            for (OrderTuple tup : tupleList) {
                // calculate the number of units that this customer should receive based on proportion
                BigDecimal unitsAppropriated = tup.getUnits().multiply(saxosUnits).divide(sumAllUnits, 2, BigDecimal.ROUND_HALF_UP);
                String customerIdentifier = tup.getCustomerIdentifier();
                Order respOrder = new Order(unitsAppropriated, saxosValue, saxosProductId);
                System.out.println("sending order to customer: "+customerIdentifier+" for product: "+saxosProductId+
                        ", with total units "+unitsAppropriated+" and unit value "+saxosValue);

                // complete the transaction for the customer
                Customer x = customerMap.get(customerIdentifier);
                x.completeTransaction(respOrder);
            }
        }
    }

    // rounds the absolute values of numbers down
    // i.e. 5.6 -> 5, -5.6 -> -5
    // if value.abs() < 1, then return 1
    private BigDecimal roundAbsDown(BigDecimal inp){
        BigDecimal result;

        if (inp.abs().compareTo(BigDecimal.ONE) == -1){
            result = BigDecimal.ONE;
        } else {
            result = inp.abs().setScale(0, BigDecimal.ROUND_FLOOR);
        }

        if (inp.signum() == -1){
            result = result.multiply(new BigDecimal("-1"));
        }

        return result;
    }

}
