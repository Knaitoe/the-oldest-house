package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;

/** 0.4.633, Second Person, spring: everything said and read in the let (docs/SECOND_PERSON_0_4_633.md flags each line as new). */
public final class SecondPersonSpringTexts {
    private SecondPersonSpringTexts(){}
    // The agent.
    public static final String AGENT_HELLO="Morning. I won't keep you. Just a look round for the landlord.";
    public static final List<String> AGENT_ROUND=List.of("Hall's fine.","Light's good at the front. They always say that about the front.","Tap doesn't drip? Good.","And the bathroom. That's the lot.");
    public static final String AGENT_COLD="Is it me, or is it cold in here?";
    public static final String COLD_KNOWN="It does that.",COLD_OLD="It's an old building.";
    public static final String AGENT_TOUCHED="Sorry, was that you? No.";
    public static final String AGENT_SAME="Look, I'll put it down as damp. Same as last year, then.";
    public static final String AGENT_UP="It'll go up in the summer. Everything does. I'll send it over.";
    public static final String AGENT_THANKS="Thanks. End of the week, then.";
    // Deciding.
    /** 0.4.640: the guest by the House's read; Marcus, who moved in, would look for somewhere bigger with them. */
    public static String thinking(String going,String guest){return going.equals("sam")?guest.equals("Marcus")?"Marcus says we could find somewhere bigger. The two of us.":guest+"'s got a spare room. "+guest+"'s got a garden.":thinking(going);}
    public static String decided(String going,String guest){return going.equals("sam")?guest.equals("Marcus")?"Marcus and me are going to look for somewhere.":"I'm going to "+guest+"'s.":decided(going);}
    public static String thinking(String going){return switch(going){
        case "sam"->"Sam's got a spare room. Sam's got a garden.";
        case "home"->"I could go home. They'd have the back room ready by Friday.";
        case "safe"->"Somewhere nobody has the address. Somewhere new, again.";
        case "write"->"I could write back. I could just write back.";
        default->"Twelve months. Same again.";};}
    public static final String OR_STAY="Or I stay.";
    public static final String SAW_GO="Is that a hint?",SAW_STAY="...You think so?";
    public static final String STAYING="I'm staying. I'll ring them in the morning.";
    public static String decided(String going){return switch(going){
        case "sam"->"I'm going to Sam's.";
        case "home"->"I'm going home.";
        case "safe"->"I'm going. I won't say where. Not even here.";
        case "write"->"I'm going to write back. And then I'm going.";
        default->"I'm going. I don't know where yet.";};}
    // The form on the mat.
    public static final String FORM="The form on the mat";
    public static List<String> form(String name){return List.of("An application for the tenancy of the flat above the launderette.","For when the current tenant leaves.","Name: "+name+".","It is in your handwriting.");}
    public static final String READ_IT="Read it all.",FACE_DOWN="Turn it face down.",LEAVE_IT="Leave it.";
    public static final List<String> READ_ALL=List.of("Current address: this one.","How long at current address: longer than the tenant.","Reason for applying: I was here first.","At the foot there is a line to sign. It is empty.");
    public static final String REREAD="Your name, in your hand.";
    // Their question.
    public static final String READS="\"For when the current tenant leaves.\" That's me.";
    public static final String ASK="Is this you?";
    public static final String NO_ANSWER="...Somebody's idea of a joke.";
    public static final String KEEN="Somebody's keen.";
    // 0.4.634, E2: the form signed, and the last day.
    public static final String SIGN_IT="Sign it.";
    public static final java.util.List<String> SIGNED=java.util.List.of("You write your name on the line at the foot. It comes out the way it always does.","The ink is still wet when the room changes.");
    public static final String LAST_DAY="The rest of the year goes by. Then a last day of it, and the flat is yours.";
    public static final java.util.List<String> SAM_HELLO=java.util.List.of("There you are.","You look well. You look like you've slept.","I can't stop. I just wanted to see you in it. The place.");
    public static final String COME_IN="Come in.",NOT_TODAY="Not today.";
    public static final String SAM_IN="Just for a minute, then. Look at you.",SAM_OUT="Next time, then. Don't be a stranger.";
    public static final String SAM_GONE="Whoever it was goes back down the stairs.";
    public static final String DARK="It gets dark. The street window is black.";
    public static final String BEHIND="In the black glass, behind you, someone stands where you used to stand.";
    public static final String THEY_ASK="...Hello?",THEY_ASK_AGAIN="Is somebody in my flat?";
    public static String answer(String word){return switch(word){
        case "YES"->"Then you were here first.";
        case "NO"->"Then who keeps writing it?";
        case "STAY"->"Stay, then.";
        case "GO"->"Go where? You live here.";
        case "HELLO"->"Hello.";
        case "SORRY"->"What for?";
        default->"...";};}
}
