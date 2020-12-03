package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class CreditCard implements Serializable {
    private int number;
    private AtomicInteger amount;


    public CreditCard(int number,int amount){
        this.number=number;
        this.amount=new AtomicInteger(amount);
    }

    public int getNumber(){
        return this.number;
    }

    public int getAmount(){
        return this.amount.get();
    }

    public void chargeAmount(int charge){
        amount.addAndGet(-charge);
    }

}
