package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.422: what the shops along Drisking's street say. Original writing. The childhood visits are the early nineties and the grown ones
 * years later, so the menu and the price cards went up in between, and the doctor's corkboard holds a later year's cards.
 */
public final class BorrascaShopTexts {
    private BorrascaShopTexts(){}
    private static ItemStack typed(String title,String author,String... pages){
        var fitted=new ArrayList<String>();
        for(String original:pages){var page=new StringBuilder();for(String word:original.split("(?<=\\s)")){if(page.length()+word.length()>145){fitted.add(page.toString().strip());page.setLength(0);}page.append(word);}if(!page.isEmpty())fitted.add(page.toString().strip());}
        return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,fitted);
    }

    /** The Hilltop Cafe's letter board. */
    static ItemStack menu(boolean later){
        return later?typed("The Hilltop Cafe","Hilltop Cafe",
                "HILLTOP CAFE\nOpen 6 to 2\nClosed Sundays\n\nCoffee 1.25\nPie, by the slice 3.50\n  cherry, pecan, chess\nA la mode 4.25",
                "Plate lunch 7.95\nGrilled cheese 4.50\nMalt 3.75\n\nWhole pies to go: call ahead.\nSorry, no checks.")
            :typed("The Hilltop Cafe","Hilltop Cafe",
                "HILLTOP CAFE\nOpen 6 to 2\nClosed Sundays\n\nCoffee .50\nPie, by the slice 1.25\n  cherry, pecan, chess\nA la mode 1.75",
                "Plate lunch 3.95\nGrilled cheese 1.95\nMalt 1.50\n\nWhole pies to go: call ahead.\nAsk about today's pie.");
    }

    /**
     * The doctor's corkboard: the cards the families sent in, a few weeks apart. 0.4.424 (owner: "the children were named after their
     * progenitor"): every baby's name begins with P or K, the Prescotts' letter or the Sheriff's, in the childhood year and years later
     * alike. Nothing on the board says so.
     */
    static ItemStack cards(int visit){
        boolean later=visit>=3;String y=later?"2009":"1992";
        String[] n=later?new String[]{"Kennedy","Paxton","Kaylee","Piper and Kade","Kingston"}:new String[]{"Kelsey Ann","Paul","Katelyn","Peyton and Kody","Kevin"};
        return typed("Cards on Dr. Hale's corkboard","Drisking families",
            "Pinned in rows, edges curling. Every card has a stork, or a rattle, or a ribbon, and every one says thank you to the doctor.",
            "PRUITT\nIt's a girl! "+n[0]+"\nMarch 3, "+y+"\nThank you, Doc.\n\nWEBB\n"+n[1]+", 7 lb 2 oz\nMarch 20, "+y+"\nWe can't thank you enough.",
            "HOLLIS\nOur daughter, "+n[2]+"\nApril 6, "+y+"\nAn answer to prayer.\n\nCORLEY\nTwins! "+n[3]+"\nApril 28, "+y+"\nThank you for everything.",
            "MADDOX\nA son, "+n[4]+"\nMay 15, "+y+"\nThank you, Dr. Hale.\n\nThe last pin holds an empty envelope, addressed to the doctor in a careful hand.",
            // 0.4.426: the couple waiting on the first visit had their girl by the second; her card is the newest on the board.
            visit==2?"TATE\nIt's a girl! Paige\nJune 1992\nThank you, Doc. We'd have been lost without you.\n\nPinned over the envelope, the newest card on the board.":"");
    }

    /** The society's framed map of the old workings. */
    static ItemStack map(){
        return typed("The old workings","Drisking Historical Society",
            "DRISKING IRON COMPANY\nShafts and drifts, 1931\n\nFour shafts in a row up the ridge, No. 4 nearest the town. Drifts run off them like roots. The haul road climbs beside them to the refinery.",
            "No. 1, at the top, is inked over twice, and beside it, in the same hand: given out 1902.\n\nA later hand has added the county road in pencil, and a small square over the head of No. 1, marked DEPOT.");
    }

    /** The price card on the general store's pegboard. */
    static ItemStack hardware(boolean later){
        return later?typed("Hardware","Drisking General Store",
                "HARDWARE\n\nFlashlight 9.99\nD batteries, pair 3.49\nRope, 50 ft 12.99\nPocketknife 14.95\nLantern mantles 1.99\n\nAsk at the counter.")
            :typed("Hardware","Drisking General Store",
                "HARDWARE\n\nFlashlight 4.99\nD batteries, pair 1.89\nRope, 50 ft 6.50\nPocketknife 7.95\nLantern mantles .89\n\nAsk at the counter.");
    }

    /** What a look at one of the shops' fittings shows, or nothing for those that only stand there. */
    /** 0.4.444: what each of the station's two pumps last sold. Kurt says the second one sticks. */
    public static String pump(int which,boolean later){
        if(which==0)return later?"The dials stopped at the last sale: 11 gallons, 31.79.":"The dials stopped at the last sale: 9 gallons, 9.81.";
        return later?"The dials stopped partway: 6 gallons, and the cents wheel caught between two numbers. This is the one that sticks.":"The dials stopped at the last sale: 4 gallons, 4.36.";
    }
    public static @Nullable String look(BorrascaShopBlock.Kind kind,int stage,boolean later){
        return switch(kind){
            case PIE_CASE->stage==1?"Three pies under the glass. One has been cut.":"Three pies under the glass: cherry, pecan and one with a lattice top.";
            case URN->"The coffee urn ticks as it heats. Its little window shows it full.";
            case PUMP,PUMP_TOP->pump(0,later);
            case PRICE->later?"Regular 2.89. Unleaded 2.99. Diesel, ask inside.":"Regular 1.09. Unleaded 1.14. Diesel, ask inside.";
            case PAYPHONE->"A payphone. Twenty-five cents for a local call. Someone has scratched a number into the steel and scratched it out again.";
            case COOLER->"Bottles of soda standing in ice water. The lid is cold to touch.";
            case CANDY->"Candy bars in rows, the good ones at the bottom where small hands can reach.";
            case CASE->"Under the glass: a pocket watch stopped at ten past four, two spoons, an arrowhead, wire spectacles. Every tag is written by hand.";
            case HELMET->"A miner's helmet with a carbide lamp on the front. The tag says No. 2 shaft.";
            case PHOTOS->"Miners at the head of No. 2, grimed with dust. A church picnic. Two men shaking hands at the depot gate, one in a Sheriff's hat.";
            case CLOCK->"The clock runs a few minutes slow.";
            case SCALE->"A doctor's scale. Beside it, pencil lines on the wall at a child's heights, each with a first name and a year.";
            case JAR->stage==1?"A few lollipops left in the bottom of the jar.":"A jar of lollipops on the desk, for good patients.";
            case EXAM->"The paper on the table has been pulled fresh.";
            case CHART->"E. F P. T O Z. The last line is too small to read from here.";
            case FOUNTAIN->"Cherry, lemon-lime, root beer and cola, on tap.";
            case MEDS->"Bottles in rows behind the counter. A card says to ask the pharmacist.";
            case RACK->"Comics in the spinner, some of them years old. It squeaks when it turns.";
            case OPEN->"OPEN, lit, in the window.";
            // 0.4.430: the station's own stock, the store's shelves and the diner's crockery.
            case OIL->later?"Quarts of motor oil and jugs of antifreeze. The front row's labels have gone pale in the sun.":"Quarts of motor oil in rows, and jugs of antifreeze on the bottom shelf.";
            case BAIT->"LIVE BAIT on the lid. Inside, cups of night crawlers in damp dirt, and a carton of crickets that chirps when the lid comes up.";
            case SNACKS->"Bags of chips on a wire rack, gone soft at the corners.";
            case CALENDAR->later?"A feed store calendar with a leaping bass on it. It is still on June.":"A feed store calendar with a leaping bass on it. Somebody has circled a Saturday.";
            case BAIT_SIGN->"WORMS and BAIT, painted by hand, with an arrow down to the freezer.";
            case TIRES->"Used tires stacked by the wall. A price is chalked on the top one and rubbed half out.";
            case GROCERIES->"Tins of soup and beans, boxes of cereal and matches, sacks of flour on the bottom shelf.";
            case CROCKERY->"Heavy diner plates, mugs with a stripe round the rim, bowls stacked three deep.";
            // 0.4.431: Kendra's bike on its kickstand (stage 0), Patrick's dropped on its side (stage 1).
            case BIKE->stage==0?"Kendra's bike, on its kickstand. The seat is let all the way down, and the chain shines with fresh oil."
                :"Patrick's bike, dropped on its side where anybody could trip on it. A baseball card is pinned to the fork so the spokes clatter.";
            default->null;
        };
    }
    /** 0.4.435: a bike by whose it is, on its kickstand or on its side. */
    static String bike(int whose,boolean lying){
        return whose==BorrascaBike.PATRICK?(lying?"Patrick's bike, dropped on its side where anybody could trip on it. A baseball card is pinned to the fork so the spokes clatter."
                :"Patrick's bike, on its kickstand. A baseball card is pinned to the fork so the spokes clatter.")
            :(lying?"Kendra's bike, on its side in the road. The seat is let all the way down, and the chain shines with fresh oil."
                :"Kendra's bike, on its kickstand. The seat is let all the way down, and the chain shines with fresh oil.");
    }
}
