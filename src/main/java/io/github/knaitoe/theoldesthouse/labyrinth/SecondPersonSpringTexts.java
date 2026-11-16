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
    public static String answer(String word){return switch(word){
        case "YES"->"Then you were here first.";
        case "NO"->"Then who keeps writing it?";
        case "STAY"->"Stay, then.";
        case "GO"->"Go where? You live here.";
        case "HELLO"->"Hello.";
        case "SORRY"->"What for?";
        default->"...";};}
}
