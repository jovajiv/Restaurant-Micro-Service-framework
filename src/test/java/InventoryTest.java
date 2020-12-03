import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.OrderResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.*;

import java.awt.print.Book;

import static org.junit.Assert.*;


public class InventoryTest {
    private Inventory inventory;
    private BookInventoryInfo book1,book2;
    private BookInventoryInfo[] list;


    @Before
    public void setUp(){
        this.inventory= Inventory.getInstance();
        this.book1=new BookInventoryInfo("yoav's_book",50,2);
        this.book2=new BookInventoryInfo("Shai's_book",100,1);
        list=new BookInventoryInfo[3];
        list[2]=new BookInventoryInfo("empty_one",1000,0);
        list[0]=book1;
        list[1]=book2;
    }

    @After
    public void tearDown(){
        this.inventory=null;
        this.book1=null;
        this.book2=null;
        this.list=null;

    }


    @Test
    public void getInstance() {
       assertTrue(this.inventory==Inventory.getInstance());

    }

   @Test
    public void load() {        //  ~~cant add test , we need some kind of getter in Inventory,  yet it is only allowed to add private fields and methods to the class.
        this.inventory.load(list);
       assertEquals(-1, inventory.checkAvailabiltyAndGetPrice("no_such_book_2"));
        assertEquals(50, inventory.checkAvailabiltyAndGetPrice(book1.getBookTitle())); //yoav's_book
       assertEquals(100, inventory.checkAvailabiltyAndGetPrice("Shai's_book"));
       assertEquals(-1, inventory.checkAvailabiltyAndGetPrice("empty_one"));
       assertEquals(-1, inventory.checkAvailabiltyAndGetPrice("no_such_book"));

    }


    @Test
    public void take() {
        inventory.load (list);
        assertEquals(2, book1.getAmountInInventory());
        assertEquals(1, book2.getAmountInInventory());
        Assert.assertEquals(OrderResult.NOT_IN_STOCK,inventory.take("empty_one"));
        assertEquals(OrderResult.SUCCESSFULLY_TAKEN,inventory.take(book1.getBookTitle()));
        assertEquals(OrderResult.SUCCESSFULLY_TAKEN,inventory.take(book2.getBookTitle()));
        assertEquals(0,book2.getAmountInInventory());
        assertEquals(OrderResult.SUCCESSFULLY_TAKEN,inventory.take(book1.getBookTitle()));
        assertEquals(0,book1.getAmountInInventory());
        assertEquals(OrderResult.NOT_IN_STOCK,inventory.take(book2.getBookTitle()));
        assertEquals(OrderResult.NOT_IN_STOCK,inventory.take(book1.getBookTitle()));
        assertEquals(0,book1.getAmountInInventory());
        assertEquals(OrderResult.NOT_IN_STOCK,inventory.take(book1.getBookTitle()));
        assertEquals(0,book1.getAmountInInventory());
    }

    @Test
    public void checkAvailabiltyAndGetPrice() {
        inventory.load (list);
        assertEquals(-1,inventory.checkAvailabiltyAndGetPrice(list[2].getBookTitle()));
        assertEquals(100,inventory.checkAvailabiltyAndGetPrice(list[1].getBookTitle()));
        assertEquals(50,inventory.checkAvailabiltyAndGetPrice(list[0].getBookTitle()));
        assertEquals(-1,inventory.checkAvailabiltyAndGetPrice("no_such_book"));

    }


}