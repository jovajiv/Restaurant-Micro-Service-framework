package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.OrderBook;

public class TakeBook implements Event {

    private OrderBook order;

    public  TakeBook(OrderBook order) {
        this.order=order;
    }

    public OrderBook getOrder() {
            return order;
        }

}
