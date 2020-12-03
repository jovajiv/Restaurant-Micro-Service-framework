import bgu.spl.mics.Future;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;



public class FutureTest {

    private Future<Object> test_future1;
    private Future <Object> test_future2;
    private Future <Object> test_future3;
    private Object Complete = new Object();

    @Before
    public void setUp(){
        test_future1 = new Future();
        test_future2 = new Future();
        test_future3 = new Future();
    }

    @After
    public void tearDown(){
        test_future1 = null;
        test_future2 = null;
        test_future3 = null;
    }

    @Test
    public void get() {
        test_future1.resolve(Complete);
        assertEquals(Complete,test_future1.get());
        test_future2.resolve(Complete);
        assertEquals(Complete,test_future2.get());
    }

    @Test
    public void resolve() {
        test_future1.resolve(Complete);
        assertTrue(test_future1.isDone()); //after resolve we expect true in isDone for this test_future
        test_future2.resolve(Complete);
        assertTrue(test_future2.isDone()); //after resolve we expect true in isDone for this test_future
        assertFalse(test_future3.isDone());
    }

    @Test
    public void isDone() {
        assertFalse(test_future1.isDone()); //didn't resolved it yet. expect false;
        assertFalse(test_future2.isDone());
        assertFalse(test_future3.isDone());
        test_future1.resolve(Complete);
        assertTrue(test_future1.isDone()); //we resolved it, we want to see true
        assertFalse(test_future2.isDone()); //didn't resolved it yet. expect false;
        test_future3.resolve(Complete);
        assertTrue(test_future3.isDone()); //we resolved it, we want to see true
    }

    @Test
    public void get1() { // can try only by opening a thread
    }
}