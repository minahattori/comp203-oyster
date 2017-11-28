import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import com.tfl.billing.TravelTracker;

public class Example {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello");
        OysterCard myCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
        OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
        OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);
        TravelTracker travelTracker = new TravelTracker();
        travelTracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);
        paddingtonReader.touch(myCard);
        //minutesPass(1);
        bakerStreetReader.touch(myCard);
        //minutesPass(1);
        bakerStreetReader.touch(myCard);
        //minutesPass(1);
        kingsCrossReader.touch(myCard);
        travelTracker.chargeAccounts();
        System.out.println("HI");
    }
    private static void minutesPass(int n) throws InterruptedException {
        Thread.sleep(n * 60 * 1000);
    }
}