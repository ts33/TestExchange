package test;

import model.Order;
import model.Customer;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


public class CustomerTest {

    Customer customer;
    String sellIdentifier = "A";
    String buyIdentifier = "B";

    @Before
    public void initialize() {
        customer = new Customer("Friendly Customer");

        // unit, value, identifier
        Order sellDeriv = new Order(new BigDecimal("10"), new BigDecimal("100"), sellIdentifier);
        Order sellTrx = new Order(new BigDecimal("-0.50"), new BigDecimal("105"), sellIdentifier);
        customer.addDerivatives(sellDeriv);
        customer.addTransactions(sellTrx);

        Order buyDeriv = new Order(new BigDecimal("10"), new BigDecimal("100"), buyIdentifier);
        Order buyTrx = new Order(new BigDecimal("0.50"), new BigDecimal("105"), buyIdentifier);
        customer.addDerivatives(buyDeriv);
        customer.addTransactions(buyTrx);
    }

    @Test
    public void sellOrderSame() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("-0.50");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, sellIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("9.50"), customer.getDerivative(sellIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(sellIdentifier));
        assertEquals(new BigDecimal("5.00"), customer.getCash());
    }

    @Test
    public void sellOrderLess() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("-0.40");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, sellIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("9.60"), customer.getDerivative(sellIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(sellIdentifier));
        assertEquals(new BigDecimal("4.00"), customer.getCash());
    }

    @Test
    public void sellOrderMore() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("-0.60");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, sellIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("9.50"), customer.getDerivative(sellIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(sellIdentifier));
        assertEquals(new BigDecimal("5.00"), customer.getCash());
    }

    @Test
    public void buyOrderSame() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("0.50");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, buyIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("10.50"), customer.getDerivative(buyIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(buyIdentifier));
        assertEquals(new BigDecimal("-5.00"), customer.getCash());
    }

    @Test
    public void buyOrderLess() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("0.40");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, sellIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("10.40"), customer.getDerivative(sellIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(sellIdentifier));
        assertEquals(new BigDecimal("-4.00"), customer.getCash());
    }

    @Test
    public void buyOrderMore() throws Exception {
        // Given
        BigDecimal unit = new BigDecimal("0.60");
        BigDecimal val = new BigDecimal("10");
        Order p = new Order(unit, val, sellIdentifier);
        customer.completeTransaction(p);

        // Then
        assertEquals(new BigDecimal("10.50"), customer.getDerivative(sellIdentifier).getUnits());
        assertEquals(null, customer.getTransaction(sellIdentifier));
        assertEquals(new BigDecimal("-5.00"), customer.getCash());
    }

}
