package model;


import java.math.BigDecimal;


public class OrderTuple {
    
    private String customerIdentifier;
    private BigDecimal units;
    
    public OrderTuple(String customerIdentifier, BigDecimal units) {
        this.customerIdentifier = customerIdentifier;
        this.units = units;
    }

    public String getCustomerIdentifier() {
        return customerIdentifier;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public String toString(){
        return "CustomerID : " + customerIdentifier + " with " + units + " units";
    }
}