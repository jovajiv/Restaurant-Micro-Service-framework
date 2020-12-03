package bgu.spl.mics.application.passiveObjects;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class BookInventoryInfo {
	//~~~~~~~~~~~~~~~~~~~fields~~~~~~~~~~~~~~~~~~~~~~~~~
	private String bookTitle;
	private int price;
	private int amount;
	//~~~~~~~~~~~~~~~~~~~constructor~~~~~~~~~~~~~~~~~~~~~~~~~
	public BookInventoryInfo (String _name, int _price, int _amount) {
		this.bookTitle = _name;
		this.price = _price;
		this.amount = _amount;
	}
	//~~~~~~~~~~~~~~~~~~~methods~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
     * Retrieves the title of this book.
     * <p>
     * @return The title of this book.   
     */
	public String getBookTitle() {
		if (this!=null)
		return this.bookTitle;

		return ""; //if null, no name.
	}

	/**
     * Retrieves the amount of books of this type in the inventory.
     * <p>
     * @return amount of available books.      
     */
	public int getAmountInInventory() {
		if (this!=null)
		return this.amount;

		return 0; //if null, 0 in inventory...
	}

	/**
     * Retrieves the price for  book.
     * <p>
     * @return the price of the book.
     */
	public int getPrice() {
		if (this!=null)
		return this.price;

		return -1; //if null no price.
	}
	public void setAmount(int newAmount) { //added by shai 8.12 to use of Inventory.
		this.amount = newAmount;
	}


}
