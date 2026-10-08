package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import java.util.regex.*;

/**
 * The words of one explorer's five staircase leaves, kept free of game classes. Every
 * sentence states a recorded fact handed in by {@link StaircaseAccount}; what varies is who
 * seems to be writing, which facts the leaves choose, how each is put and the order they come in.
 */
public final class StaircaseProse {
    private StaircaseProse() {}

    /** Four narrators, each with its own hand and its own way of laying out a leaf. */
    public enum Voice {
        WITNESS("WILL"), LETTER("PELAFINA"), SURVEY("ZAMPANO"), LEDGER("PLAIN");
        public final String hand;
        Voice(String hand) { this.hand = hand; }
    }

    /** A written account: its narrator, the title leaf that stays in the binding, and five loose leaves. */
    public record Story(Voice voice, String front, List<String> leaves) {}
    /** One recorded fact: a kind from the table below, its native count, and any name it carries. */
    public record Fact(Kind kind, long count, String value) {}

    public record Kind(String id, String group, double base, double typical, String ledger, String... lines) {}

    // ---------------------------------------------------------------- facts

    private static final List<Kind> KINDS = List.of(
        new Kind("walked","move",1.1,5000,"Walked: {n|metre|metres}",
            "You have walked {n|metre|metres}, all told. The count came down these stairs with you.",
            "{n|metre|metres} on foot. Somewhere along them, going on became a habit.",
            "Your feet kept a count you never asked for: {n|metre|metres}."),
        new Kind("sprinted","move",1.0,3000,"Ran: {n|metre|metres}",
            "You ran {n|metre|metres}. Hurry is written into you.",
            "{n|metre|metres} at a run. The House has noticed how quickly you can leave.",
            "You sprinted {n|metre|metres}. On these stairs, running is how people fall."),
        new Kind("swum","move",1.2,500,"Swam: {n|metre|metres}",
            "You swam {n|metre|metres}. Water held you up and asked for nothing back.",
            "{n|metre|metres} through water. You know how to keep your head above it.",
            "You swam {n|metre|metres}: a long way to trust something you cannot stand on."),
        new Kind("boated","move",1.4,1000,"Rowed: {n|metre|metres}",
            "You rowed {n|metre|metres}. A boat is a thin floor over deep water. You are used to that.",
            "{n|metre|metres} by boat, with a few planks between you and the dark water.",
            "You crossed {n|metre|metres} of water by boat. The oars learned your hands."),
        new Kind("rode","move",1.5,1000,"Rode: {n|metre|metres}",
            "You rode {n|metre|metres}. Something carried you, and you let it.",
            "{n|metre|metres} on horseback. You have trusted another heartbeat with your weight.",
            "You covered {n|metre|metres} in the saddle. Down here there are only your own legs."),
        new Kind("flew","move",2.0,2000,"Flew: {n|metre|metres}",
            "You flew {n|metre|metres}. These stairs only go one way, and it is not up.",
            "{n|metre|metres} on wings. Down here the air has nothing to hold you with.",
            "You glided {n|metre|metres} once the ground let go of you. Remember how that felt."),
        new Kind("fell","move",1.0,200,"Fell: {n|metre|metres}",
            "You fell {n|metre|metres}, all told. The ground was always there to meet you.",
            "{n|metre|metres} of falling, counted a little at a time. Keep to the rail.",
            "You have fallen {n|metre|metres}. The rail is there for a reason."),
        new Kind("climbed","move",1.0,100,"Climbed: {n|metre|metres}",
            "You climbed {n|metre|metres}, hand over hand. These stairs ask that only on the way back.",
            "{n|metre|metres} climbed. You know how to go up when the way down is wrong.",
            "Rung by rung, {n|metre|metres} of climbing. Up is a direction you remember."),
        new Kind("deaths","loss",2.2,5,"Died: {times}",
            "You died {times}. Each time, the world gave you back.",
            "Death had you {times}, and let go {times}.",
            "You have died {times}. The House is not the first place to keep you a while."),
        new Kind("kills","harm",1.5,200,"Killed: {n|creature|creatures}",
            "You killed {n|creature|creatures}. The count is kept. The reasons are not.",
            "{n|creature|creatures}, killed by your hand. The fire will not ask which you regret.",
            "Your hands have ended {n|life|lives}. Down here, that number is very quiet."),
        new Kind("broke","harm",1.8,1,"Last broken: {x}",
            "Your last break in the Overworld: {x}. Something stood there. Now a space does.",
            "{X}, broken: the last block you broke in the Overworld.",
            "The Overworld kept a record of what you broke. The latest entry is {x}."),
        new Kind("built","make",2.0,1,"Last placed: {x}",
            "Your last block in the Overworld: {x}. Up there, it may still stand.",
            "{X}, placed by your hand: your latest placement in the Overworld.",
            "You build. The last proof of it in the Overworld is {x}."),
        new Kind("bread","make",1.3,30,"Bread: {n|loaf|loaves}",
            "You made {n|loaf|loaves} of bread. Your hands know work that keeps a person alive.",
            "{n|loaf|loaves} of bread, made by you. Nothing down here will feed you like that.",
            "You baked bread {times}. On these stairs, no one is baking any for you."),
        new Kind("enchanted","make",1.5,10,"Enchanted: {n|thing|things}",
            "You enchanted {n|thing|things}. You have bargained with strange light before.",
            "{n|enchantment|enchantments}, laid down by your hand. Some words cost more than ink.",
            "You have enchanted {times}. The fire below is the plainest magic you will meet."),
        new Kind("bred","care",1.6,20,"Bred: {times}",
            "You bred animals {times}. There are lives in the world that began with you.",
            "Animals were born {times} because you fed their parents. Care is in your record.",
            "You coaxed animals into families {times}. That kind of patience is rare."),
        new Kind("cared","care",3.0,1,"Touched: {x}",
            "You put your hand on {x}. The ink keeps the touch, not where it led.",
            "{X} let you close. The House remembers who you were gentle with.",
            "Of everything in the House, you stopped for {x}."),
        new Kind("fish","care",1.8,20,"Fish: {n}",
            "You caught {n|fish|fish}. Patience is in your record, the kind that waits at water.",
            "{n|fish|fish}, drawn up out of the dark. You know how to wait for what lies below.",
            "You have fished {times}. The dark down here is deeper, and nothing bites."),
        new Kind("cake","play",1.6,7,"Cake: {n|slice|slices}",
            "You ate {n|slice|slices} of cake. Once, there was reason to celebrate.",
            "{n|slice|slices} of cake. Not every page of you is dark.",
            "Cake, {times}. The House has never been invited to a party."),
        new Kind("flowers","play",1.6,5,"Potted: {n|flower|flowers}",
            "You potted {n|flower|flowers}. You make small things grow in small places.",
            "{n|flower|flowers} in pots because of you. Nothing grows on these stairs.",
            "You gave flowers a pot {times}. Small kindnesses count too."),
        new Kind("music","play",1.6,20,"Music: {times}",
            "You played music {times}. The stairwell would like to hear it.",
            "You set music playing {times}. Down here only the fires make a sound.",
            "You made sound for its own sake {times}. That is a kind of company."),
        new Kind("bells","play",1.5,5,"Bells: {n}",
            "You rang a bell {times}. Someone, somewhere, was meant to hear.",
            "{n|bell|bells} rung by you. The House does not answer bells.",
            "You have rung bells {times}. Listen. Nothing rings down here."),
        new Kind("traded","trade",1.4,30,"Trades: {n}",
            "You traded with villagers {times}. For a while, things had an agreed value.",
            "{n|bargain|bargains} struck with villagers. You know how to give to get.",
            "You have traded {times}. The fire takes paper and gives light. That is a trade too."),
        new Kind("slept","rest",1.2,20,"Nights in a bed: {n}",
            "You slept in a bed {times}. Not every darkness needed a fire.",
            "{n|night|nights} in a bed. Every one of them ended in waking.",
            "You have lain down and let the night pass {times}. There is no night down here."),
        new Kind("hours","rest",0.9,20,"Hours: {n}",
            "You have spent {n|hour|hours} in this world. A few minutes of it are on these pages.",
            "{n|hour|hours} of play, all told. The stairs keep their own clock.",
            "Your record holds {n|hour|hours} of you. Five leaves cannot hold much of that."),
        new Kind("retreat","house",2.6,1,"Left unfinished: {x}",
            "You turned back from {x} with its story unfinished. The door let you go.",
            "In {x} you chose to leave before the end. That is written here too.",
            "You walked out of {x} and left it open."),
        new Kind("lost","house",3.0,1,"With the Mother: {x}",
            "Something of yours is still in the Mother's keeping: {x}.",
            "{X} is in the den, and not with you. The House still counts it as yours.",
            "The Mother of Strays has {x}, which was yours. She keeps what she takes."),
        new Kind("manor","house",1.8,3,"Slept in the manor: {times}",
            "You slept inside the manor {times}. The House has held you still, not only moved you.",
            "You closed your eyes in the manor {times}, and the House let you wake.",
            "{Times} you slept under the House's roof. It watched the whole night."),
        new Kind("letters","house",1.6,5,"Letters read: {n}",
            "You read {n|letter|letters} here. For a moment, the House had a reader.",
            "{n|letter|letters}, opened and read by you. Someone was writing to be read.",
            "You have read {n|letter|letters} in the House. This page is not a letter. It is closer."),
        new Kind("deepest","house",1.5,12,"Doors deep: {n}",
            "You went {n|door|doors} deep before these stairs. Each one closed behind you.",
            "{n|door|doors} down. The House remembers every threshold you crossed.",
            "You opened your way {n|door|doors} into the House. These stairs are the deepest door."));

    private static final String[] QUIET = {
        "This leaf is nearly blank. Whatever you did before the House, you did quietly.",
        "The ink thins here. Some of a life is not the kind that gets counted.",
        "A clean page, creased once down the middle, as if folded around something small.",
        "Only a few words survive the damp: you were here, and you kept going.",
        "The page was written on and scraped clean. The pen dug in anyway.",
        "Here the writing stops mid-word, as if whoever held the pen heard you coming."};

    private static final Map<String,String[]> TITLES = Map.of(
        "move",new String[]{"The road","Distance","Feet"}, "loss",new String[]{"Returns","The count"},
        "harm",new String[]{"Hands","What fell"}, "make",new String[]{"Work","What you made"},
        "care",new String[]{"Gentleness","What you kept"}, "play",new String[]{"Small joys","Noise"},
        "trade",new String[]{"Exchange","Bargains"}, "rest",new String[]{"Rest","Nights"},
        "house",new String[]{"The House","Thresholds"}, "quiet",new String[]{"Blank","Margin"});

    private static final String[] WITNESS_CLOSE = {"The stairs went on listening.","Somewhere below, a fire is waiting for this.",
        "The ink dried before you could argue.","That is the page. The rest stayed with you.","Read it once more. Then it is fuel.",
        "It is less than you were. It is what was written."};
    private static final String[] WITNESS_END = {"All of you that fits on five leaves.\n\nYou may still go back.",
        "When this burns, the stairs open.\n\nYou may still go back."};
    private static final String[] LETTER_OPEN = {"Dear {name},","My dear {name},","{name},","Dearest,","Again, {name},"};
    private static final String[] LETTER_CLOSE = {"Go carefully.","Keep the fire small.","Burn this when you must.","I am still counting.",
        "Yours, in the walls","Write back, if stairs allow."};
    private static final String[] LETTER_END = {"There is no more paper. Go on, or go home. I would forgive either.",
        "My last leaf. What is below, you meet with your own hands."};
    private static final String[] SURVEY_NOTE = {"[Figure exact. Meaning disputed.]","[Cf. the stairs, which no tape has measured.]",
        "[Margin scorched.]","[Underlined twice in another hand.]","[The rest of the line is illegible.]","[See the next landing.]"};
    private static final String[] SURVEY_END = {"[End of survey. Depth below unmeasured.]","[Final leaf. The staircase defies the tape.]"};
    private static final String[] LEDGER_NOTE = {"Carried forward.","Balance unknown.","Checked twice.","No receipt given.","Entered in the dark.",
        "Payable on the next landing."};
    private static final String[] LEDGER_END = {"Account closed. You may still go back.","Nothing further owed. What remains is not on paper."};
    private static final String[] ROMAN = {"I","II","III","IV","V"};

    // ---------------------------------------------------------------- places and names

    private static final Map<String,String> PLACES = new HashMap<>();
    static {
        String[][] names = {
            {"junction","the junction"},{"gray_corridor","the gray corridor"},{"floorboards","the room of loose boards"},
            {"red_room","the red room"},{"hide_and_clap","the hide-and-clap room"},{"long_hallway","the long hallway"},
            {"spiral_stair","the spiral stair"},{"hotel_hallway","the hotel corridor"},{"model_home","the model home"},
            {"harrigan","Mr. Harrigan's study"},{"flooded_passage","the flooded passage"},{"fractured_walkway","the broken walkway"},
            {"compression_passage","the narrowing passage"},{"false_distance","the corridor that lied"},{"light_sink","the room that ate light"},
            {"moving_threshold","the moving doorway"},{"duplicate_passage","the doubled passage"},{"gravity_drift","the leaning passage"},
            {"explorer_camp","the explorers' camp"},{"mother_of_strays","the Mother's den"},{"folded_maze","the folded maze"},
            {"deep_maze","the deep maze"},{"abyss_maze","the lowest maze"},{"straight_hall","a straight hall"},{"bent_hall","a bent hall"},
            {"cross_hall","a crossing hall"},{"quiet_room","the quiet room"},{"drowned_town","the drowned town"},
            {"preserved_cave","the cave under the lake"},{"shallows","the shallows"},{"phone_canoe","the canoe"},
            {"goatman","the door in the woods"},{"ted_caver","the narrow cave"},{"zampano_courtyard","the blind man's courtyard"},
            {"whale","the attic of letters"},{"barn_well","the well by the barn"},{"plain","the open plain"},{"hospital","the night ward"},
            {"karen_room","Karen's room"},{"holloway_camp","Holloway's camp"},{"seance","the seance parlour"},
            {"wallpaper_nursery","the yellow nursery"},{"blind_stretch","the blind stretch"},{"hotel","the hotel"},
            {"hotel_grounds","the hotel grounds"},{"hill_nursery","the Hill House nursery"},{"miniatures","the miniatures workshop"},
            {"masque","the seven rooms"},{"usher","the Usher vault"},{"winchester","the Winchester wing"},{"child_room","the child's room"},
            {"crimson_hall","the crimson hall"},{"bly_route","the Lady's route"},{"elk_lot","the roadside lot"},
            {"elk_fan","the room with the fan"},{"mapping_interior","the crawlspace house"},{"holy_rabbit","the rabbit's house"},
            {"confession","the confession"},{"elk_carcasses","the cut under the ridge"},{"costume_night","costume night"},
            {"movie_night","movie night"},{"winter_lake","the frozen lake"},{"camp_blood","Camp Blood"},
            {"devils_rock","the diary room"},{"wheel","the wheel of rooms"},{"ghosts_set","the haunted set"},
            {"end_world_cabin","the cabin at the end"},{"family_copy","your family's copied home"},{"old_cabin","the old man's cabin"},
            {"stone_gallery","a stone gallery"},{"stone_crossing","a stone crossing"},{"stone_descent","a stone stair"},
            {"hallway_end","the impossible hallway"}};
        for (String[] n : names) PLACES.put(n[0], n[1]);
    }
    /** A room's name as prose, never its internal identifier. */
    public static String place(String id) {
        String clean = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        return PLACES.getOrDefault(clean, "a room the House has not named");
    }
    public static int longestPlace() { return PLACES.values().stream().mapToInt(String::length).max().orElse(0); }
    /** A fact of a known kind, or nothing when the record holds none of it. */
    public static Optional<Fact> fact(String id, long count, String value) {
        if (count <= 0 && value.isBlank()) return Optional.empty();
        return KINDS.stream().filter(k -> k.id.equals(id)).findFirst().map(k -> new Fact(k, Math.max(1, count), shortName(value)));
    }
    static String shortName(String text) {
        String clean = text.replaceAll("[\\p{Cntrl}]", "").trim();
        int end = clean.offsetByCodePoints(0, Math.min(24, clean.codePointCount(0, clean.length())));
        return clean.substring(0, end);
    }

    /** Five linked chapters. Counts choose memories; the prose does not reproduce a statistics screen. */
    public static Story compose(String name, List<Fact> facts, long seed) {
        var rng=new Random(mix(seed));Voice voice=Voice.values()[rng.nextInt(Voice.values().length)];
        var remaining=new ArrayList<>(facts);
        remaining.sort(Comparator.comparingDouble((Fact f)->f.kind.base*(1+Math.min(1,f.count/f.kind.typical))).reversed());
        String[][] groups={{"move"},{"make","care","play","trade"},{"loss","harm","rest"},{"house"}};
        String[] titles={"I. Outside","II. The hands","III. What happened","IV. The door","V. Below"};
        String[] opening={"The first sheet catches on the cover.","The corner you folded is still warm.","There is a thumbprint beside the next line.","The writing reaches the fold.","A line continues onto the back of the sheet."};
        String[] quiet={"No place has been written beside your name. The stairs have room for it.",
            "Here the writer leaves a space for what your hands have done. It has not been filled.",
            "This part has been left unwritten. I cannot give you a grief because I need one.",
            "You are here now. The page has caught up with the person carrying it."};
        String[] close={"You lift the caught corner and go on.","You flatten it with the same hand.","You turn the sheet without covering the print.","The next word is underneath your thumb."};
        var leaves=new ArrayList<String>();
        for(int i=0;i<4;i++){
            final int chapter=i;
            Fact chosen=remaining.stream().filter(f->List.of(groups[chapter]).contains(f.kind.group)).findFirst().orElse(null);
            if(chosen==null&&i<3)chosen=remaining.stream().filter(f->!f.kind.group.equals("house")).findFirst().orElse(null);
            String memory=chosen==null?quiet[i]:memory(chosen);if(chosen!=null)remaining.remove(chosen);
            String line=switch(voice){case WITNESS->opening[i];case LETTER->i==0?"Dear "+name+",":opening[i];case SURVEY->i==0?"The name on the cover is "+name+".":opening[i];case LEDGER->i==0?"I entered your name: "+name+".":opening[i];};
            leaves.add(titles[i]+"\n\n"+line+" "+memory+"\n\n"+close[i]);
        }
        String ending=switch(voice){
            case WITNESS->"The lower corner is gone. You turn the paper and find the thumbprint coming through. You have carried it down with you. The last word was under it: return.";
            case LETTER->"I nearly wrote come home. I do not know where that is for you. The corner has burned through; your thumb shows behind the paper. Come back to whoever is holding it.";
            case SURVEY->"The fold has opened. On its other side the writer drew a hand, then rubbed it out. Your own hand fills the space. Above you, the staircase still has a way back.";
            case LEDGER->"I left room for a final entry. The paper split along the fold before I could make it. Through the slit I can see your hand. You are still carrying the book.";};
        leaves.add(titles[4]+"\n\n"+ending);
        return new Story(voice,front(voice,name),List.copyOf(leaves));
    }
    private static String memory(Fact f){
        String value=f.value;
        return switch(f.kind.id){
            case "walked"->"You have travelled on foot. Here your boots bring you round the same open shaft.";
            case "sprinted"->"You have run. Here the rail bends ahead of you, and the next landing stays below.";
            case "swum"->"You have swum. The page has no water on it, but your fingers hesitate at the dark edge.";
            case "boated"->"You have travelled by boat. You turn the paper sideways, as if its narrow fold might hold a seat.";
            case "rode"->"You have ridden a horse. Here you have to carry your own weight down every tread.";
            case "flew"->"You have flown. There is enough empty air here to make that a difficult thing to remember.";
            case "fell"->"You have fallen. Your hand settles over the split in the paper.";
            case "climbed"->"You have climbed. You look up at the way you came; the rail runs out of sight.";
            case "deaths"->f.count==1?"You died, and returned. The writer crossed out the word final.":"You have died and returned. The writer keeps crossing out the word final.";
            case "kills"->"You have killed. A blot obscures what was written after that. You hold the page to the light.";
            case "broke"->"The last Overworld block you broke was "+value+". The writer has shaded its empty square.";
            case "built"->"You placed "+value+" in the Overworld. The writer drew its edges before drawing this staircase.";
            case "bread"->"You have made bread. Beside the line is a small oval, cut down the middle.";
            case "enchanted"->"You have laid an enchantment on something. Here you keep the written side away from the flame.";
            case "bred"->"You have bred animals. The writer began a second name beneath the first, and stopped.";
            case "cared"->"You stopped for "+value+". That name is written carefully. There is no blot beside it.";
            case "fish"->"You have caught fish. A curved stroke hangs from the margin like a hook.";
            case "cake"->"You have eaten cake. The writer drew a plate and left a piece on it.";
            case "flowers"->"You have potted flowers. There is a pot in the margin; the stem runs into the next line.";
            case "music"->"You have played music. Here you pause over a line of notes with no instrument named.";
            case "bells"->"You have rung a bell. The writer pressed so hard on that word that you can feel it on the back.";
            case "traded"->"You have traded with villagers. Two hands meet at the fold. Neither has been drawn empty.";
            case "slept"->"You have slept in a bed. The writer left the blanket open on one side.";
            case "hours"->"You have spent time in this world. It takes you less than a minute to turn this page.";
            case "lost"->"The Mother still holds "+value+". The name runs off the page. You unfold the corner to read it.";
            case "retreat"->"You returned safely from "+value+". The line that led there has been drawn back toward you.";
            case "manor"->"You have slept in the manor. The bed in the margin has a door beside it, standing open.";
            case "letters"->"You have read the House's letters. A sentence on this page has been copied in a different hand.";
            case "deepest"->"You have gone farther into the House. The writer has run out of room and turned the paper.";
            default->"The page has room for a memory that has not been written.";};
    }

    private static String frame(Voice voice, int leaf, String group, String body, String close, String opener, String name, Random rng) {
        return switch (voice) {
            case WITNESS -> ROMAN[leaf] + ". " + pick(TITLES.get(group), rng) + "\n\n" + body + "\n\n" + close;
            case LETTER -> opener.replace("{name}", name) + "\n\n" + body + "\n\n" + close;
            case SURVEY -> "Leaf " + (leaf + 1) + ".\n\n" + body + "\n\n" + close;
            case LEDGER -> "Leaf " + (leaf + 1) + " of 5\n\n" + body + "\n\n" + close;
        };
    }
    private static String front(Voice voice, String name) {
        return "HOUSE OF LEAVES\n\n"+name+"\n\nA strip of paper remains in the stitching. On its scorched edge someone wrote: I kept the cover.";
    }
    /**
     * The title leaf for an account saved before its title was stored. Those books have always shown
     * these exact words; a saved original keeps them, whatever new books now say.
     */
    public static String legacyFront(String name) {
        return "HOUSE OF LEAVES\n\nas found by " + name + "\n\nIts five leaves are loose on the stairs below, one to a flight. Bind each here, then give it to the next fire.";
    }

    private static List<Fact> diverse(List<Fact> sorted, int wanted) {
        var picked = new ArrayList<Fact>(); var groups = new HashSet<String>();
        for (var f : sorted) if (picked.size() < wanted && groups.add(f.kind.group)) picked.add(f);
        for (var f : sorted) if (picked.size() < wanted && !picked.contains(f)) picked.add(f);
        return picked;
    }
    private static String sentence(Fact f, long seed, String name) {
        int variant = Math.floorMod(new Random(seed ^ f.kind.id.hashCode() * 0x9E3779B97F4A7C15L).nextInt(), f.kind.lines.length);
        return fill(f.kind.lines[variant], f, name);
    }
    private static final Pattern PLURAL = Pattern.compile("\\{n\\|([^|}]*)\\|([^}]*)\\}");
    private static String fill(String template, Fact f, String name) {
        Matcher m = PLURAL.matcher(template); var out = new StringBuilder();
        while (m.find()) m.appendReplacement(out, Matcher.quoteReplacement(number(f.count) + " " + (f.count == 1 ? m.group(1) : m.group(2))));
        m.appendTail(out);
        String text = out.toString().replace("{n}", number(f.count)).replace("{Times}", capital(times(f.count))).replace("{times}", times(f.count))
            .replace("{X}", capital(f.value)).replace("{x}", f.value).replace("{name}", name);
        return capital(text);
    }
    /** Spreads nearby seeds apart; java.util.Random's first draws follow small seeds too closely. */
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L; z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL; return z ^ (z >>> 31);
    }
    private static String number(long n) { return String.format(Locale.ROOT, "%,d", n); }
    private static String times(long n) { return n == 1 ? "once" : n == 2 ? "twice" : number(n) + " times"; }
    private static String capital(String s) { return s.isEmpty() ? s : s.substring(0, s.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT) + s.substring(s.offsetByCodePoints(0, 1)); }
    private static <T> T pick(T[] options, Random rng) { return options[rng.nextInt(options.length)]; }
    private static List<String> shuffled(String[] options, Random rng) { var list = new ArrayList<>(List.of(options)); Collections.shuffle(list, rng); return list; }

    public static List<List<String>> specimens() {
        String name = "W".repeat(16), wide = "W".repeat(24), longPlace = PLACES.values().stream().max(Comparator.comparingInt(String::length)).orElseThrow();
        long max = Integer.MAX_VALUE;
        var books = new ArrayList<List<String>>();
        for (Voice voice : Voice.values()) {
            var pages = new ArrayList<String>(); pages.add(front(voice, name));
            String[] closes = switch (voice) { case WITNESS -> WITNESS_CLOSE; case LETTER -> LETTER_CLOSE; case SURVEY -> SURVEY_NOTE; case LEDGER -> LEDGER_NOTE; };
            String[] ends = switch (voice) { case WITNESS -> WITNESS_END; case LETTER -> LETTER_END; case SURVEY -> SURVEY_END; case LEDGER -> LEDGER_END; };
            String longestClose = Arrays.stream(closes).max(Comparator.comparingInt(String::length)).orElseThrow();
            String longestTitle = TITLES.values().stream().flatMap(Arrays::stream).max(Comparator.comparingInt(String::length)).orElseThrow();
            String opener = Arrays.stream(LETTER_OPEN).max(Comparator.comparingInt(s -> s.replace("{name}", name).length())).orElseThrow();
            var rng = new Random(0) { @Override public int nextInt(int bound) { return 0; } };
            for (Kind kind : KINDS) {
                Fact f = new Fact(kind, max, kind.id.equals("retreat") ? longPlace : wide);
                if (voice == Voice.LEDGER) {
                    String two = fill(kind.ledger, f, name) + "\n" + fill(KINDS.getFirst().ledger, new Fact(KINDS.getFirst(), max, wide), name);
                    for (String close : closes) pages.add("Leaf 4 of 5\n\n" + two + "\n\n" + close);
                    if (kind.group.equals("house")) for (String end : ends) pages.add("Leaf 5 of 5\n\n" + fill(kind.ledger, f, name) + "\n\n" + end);
                    continue;
                }
                for (String line : kind.lines) {
                    String body = fill(line, f, name);
                    pages.add(voice == Voice.WITNESS ? "IV. " + longestTitle + "\n\n" + body + "\n\n" + longestClose
                        : voice == Voice.LETTER ? opener.replace("{name}", name) + "\n\n" + body + "\n\n" + longestClose
                        : "Leaf 4.\n\n" + body + "\n\n" + longestClose);
                    // Only the House's own facts close an account; other kinds never reach the fifth leaf.
                    if (kind.group.equals("house")) for (String end : ends) pages.add(frame(voice, 4, "house", body, end, opener, name, rng));
                }
            }
            for (String q : QUIET) for (String end : ends) pages.add(frame(voice, 4, "quiet", q, end, opener, name, rng));
            books.add(pages);
        }
        // New accounts, including every factual branch, use the same native book-width proof.
        for(Voice voice:Voice.values()){
            var pages=books.get(voice.ordinal());
            for(Kind kind:KINDS){var f=new Fact(kind,max,kind.id.equals("retreat")?longPlace:wide);
                pages.add("IV. The door\n\nThe writing reaches the fold. "+memory(f)+"\n\nThe next word is underneath your thumb.");}
            for(long seed=0;seed<24;seed++){var account=compose(name,List.of(),seed);if(account.voice==voice){pages.add(account.front);pages.addAll(account.leaves);break;}}
        }
        return books;
    }
}
