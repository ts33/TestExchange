package controller;

import model.Customer;
import model.Order;

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

    public boolean allCustomersSettled(){
        boolean result = true;
        for (Customer x: customerMap.values()){
            if (x.getTransactions().size() > 0){
                result = false;
            }
        }
        return result;
    }

    public boolean allCustomersPositiveDerivative(){
        boolean result = true;
        for (Customer x: customerMap.values()){
            List<Order> derivatives = x.getDerivatives();
            for (Order o: derivatives){
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
    public void settleAllCustomersTransactions() throws Exception{
        for (Customer x: customerMap.values()){
            List<Order> orders = x.getTransactions();
            for (Order o: orders){
                x.completeTransaction(o);
            }
        }
    }


}
