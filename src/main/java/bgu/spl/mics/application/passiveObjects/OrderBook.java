package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;

public class OrderBook implements Comparable<OrderBook>, Serializable {
    private int tick;
    private String bookTitle;

    public OrderBook(String bookTitle,int tick){
        this.bookTitle=bookTitle;
        this.tick=tick;
    }


    public String getBookTitle() {
        return bookTitle;
    }


    public int getTick() {
        return tick;
    }

    @Override
    public int compareTo(OrderBook o) {
        return this.getTick()-o.getTick();

    }
}
