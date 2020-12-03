package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderBook;

public class BookOrderEvent implements Event {
    private OrderBook order;
    private Customer customer;


    public BookOrderEvent (OrderBook order, Customer customer) {
        this.order=order;
        this.customer = customer;
    }

    public OrderBook getOrder() {
            return order;
        }

    public Customer getCustomer() {
        return customer;
    }
}

