package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

public class OrderController {
    private HashMap<String, BigDecimal> orderMap;

    public OrderController(){
        String csvFile = "src/resources/orders.csv";
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

    public BigDecimal getValue(String orderName){
        return orderMap.get(orderName);
    }
}
