package controller;

import model.Order;
import model.SaxosReceipt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SaxosController {

    private HashMap<String, BigDecimal> orderMap;
    private BigDecimal transactionCost = new BigDecimal("5");

    public SaxosController(){
        String csvFile = "src/resources/saxos.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        orderMap = new HashMap<>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] orders = line.split(cvsSplitBy);
                orderMap.put(orders[0], new BigDecimal(orders[1]));


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

    public HashMap<String, BigDecimal> getOrderMap() {
        return orderMap;
    }

    public SaxosReceipt parseOrder(Order order) throws Exception{
        BigDecimal unit = order.getUnits().abs();
        if (unit.signum() == 0 || unit.scale() <= 0 || unit.stripTrailingZeros().scale() <= 0){
            BigDecimal value = orderMap.get(order.getIdentifier());
            BigDecimal totalAmount = (value.multiply(unit)).subtract(transactionCost);
            SaxosReceipt result = new SaxosReceipt(order.getIdentifier(), totalAmount, order.getUnits());
            return result;
        } else {
            throw new Exception("Not integer");
        }
    }

    public List<SaxosReceipt> processOrder(List<Order> orders) throws Exception {
        ArrayList<SaxosReceipt> newList = new ArrayList<>();
        for (Order x: orders){
            newList.add(parseOrder(x));
        }
        return newList;
    }
}
