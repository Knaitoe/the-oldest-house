package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The listener's questions (0.4.121). After each chapter he has dictated, he asks the person holding the pen one question,
 * and the reader answers by typing in chat. The answer never reaches public chat: only the reader sees their own words and
 * the fragment he repeats while he writes it down. Thirty seconds of silence in front of him is written down as that. The
 * sealed journal is made of the questions and the reader's own answers. 0.4.123: once a question is settled he rests a
 * moment and goes on to the next chapter in the same sitting, rather than falling silent until the reader's next visit.
 * 0.4.275 (docs/CONFESSION_0_4_275.md): he asks in the conversation panel, and the answers offered come from what the House
 * remembers of the reader (ConfessionMemory). A true one is written as it is said. An untrue one is written too, and where the
 * House knows better a line in another hand appears under it. Saying nothing is always offered. Typed chat is still heard, as
 * the reader's own words, and the House corrects nothing it did not see.
 */
public final class ConfessionAnswers {
    private ConfessionAnswers() {}

    public static final String[] QUESTIONS = {
        "What name did you come here under?",
        "Which door here did you open first?",
        "What did you take because nobody stopped you?",
        "Who is missing from your account?",
        "When you tell it, who do you tell it to?",
        "How many? Say the number.",
        "Whose name would you rather I did not write?",
        "Now say it in your own words. What did you do?"};
    /** How the journal records a question left unanswered in front of him. */
    public static final String SILENCE = "(did not answer)";
    /** Thirty seconds, counted only while the reader stands before him. */
    public static final int SILENCE_TICKS = 600;
    static final int LONGEST = 180;
    /** The pause between one chapter's answer and his next words, counted while the reader stands before him. */
    public static final int REST_TICKS = 60;

    /**
     * Whether this reader is in the middle of a chapter with him, so anything they say in chat is said to him alone. While he
     * rests between chapters, ordinary chat is the reader's own again.
     */
    public static boolean engaged(CompoundTag own) {
        return own.hasUUID("Journal") && own.getInt("Chapter") < LiteraryTexts.CONFESSION.length && own.getInt("ChapterVisit") != own.getInt("Visit")
            && own.getInt("RestTicks") <= 0;
    }

    /**
     * A chat line from a reader in the confession. Returns whether it was taken out of public chat: an answer to the open
     * question is written down and echoed back in fragment; anything said before he asks is held back too, and he says so.
     */
    public static boolean heard(ServerPlayer p, CompoundTag own, String raw) {
        if (!engaged(own)) return false;
        String answer = clean(raw);
        if (answer.isEmpty()) return true;
        if (!own.getBoolean("Asked") || own.contains("Answer")) {
            p.displayClientMessage(Component.literal("The listener lifts a finger from the page: not yet.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
            return true;
        }
        // Typed words are the reader's own: written as said, with nothing under them.
        own.putString("Answer", answer);
        own.putString("Correction", "");
        String key = key(own.getInt("Chapter"));
        if (!Talk.key(p).equals(key)) Talk.begin(p, key, ConfessionMemory.LISTENER);
        Talk.line(p, "", answer);
        return true;
    }

    /** The panel's key for one chapter's question. */
    public static String key(int chapter) { return "confession_" + chapter; }

    /** Opens (or reopens) the chapter's question in the panel from the answers offered when he asked it. */
    static void offer(ServerPlayer p, CompoundTag own, int chapter) {
        String question = QUESTIONS[Math.min(chapter, QUESTIONS.length - 1)];
        var offers = own.getList("Offers", Tag.TAG_COMPOUND);
        var replies = new ArrayList<Talk.Reply>();
        for (int i = 0; i < offers.size(); i++) {
            var offer = ConfessionMemory.Offer.load(offers.getCompound(i));
            replies.add(Talk.reply(offer.label(), q -> choose(q, chapter, offer)));
        }
        Talk.begin(p, key(chapter), ConfessionMemory.LISTENER);
        Talk.line(p, ConfessionMemory.LISTENER, question);
        Talk.ask(p, replies);
    }

    /** A reply chosen in the panel: taken down at his next pen stroke, with the House's line if it has one. */
    private static void choose(ServerPlayer p, int chapter, ConfessionMemory.Offer offer) {
        var d = LabyrinthData.get(p.server);
        var own = LiteraryVignettes.personal(d, p.getUUID(), LabyrinthPlace.CONFESSION);
        if (own.getInt("Chapter") != chapter || !own.getBoolean("Asked") || own.contains("Answer")) return;
        own.putString("Answer", offer.written());
        own.putString("Correction", offer.correction());
        LiteraryVignettes.save(d, p.getUUID(), LabyrinthPlace.CONFESSION, own);
    }

    /** Whether the reader's own Talk panel is open on this chapter's question. */
    public static boolean asking(ServerPlayer p, CompoundTag own) {
        return own.getBoolean("Asked") && !own.contains("Answer") && Talk.waiting(p, key(own.getInt("Chapter")));
    }

    /**
     * After the chapter's sentence is down: ask once, then wait for the answer or for the silence. Returns true when the
     * chapter's question is settled and its page written; false while he is still waiting. Called every fifteen ticks.
     */
    public static boolean settled(ServerPlayer p, LiteraryActor visitor, CompoundTag own, int chapter) {
        String question = QUESTIONS[Math.min(chapter, QUESTIONS.length - 1)];
        if (!own.getBoolean("Asked")) {
            own.putBoolean("Asked", true);
            own.putInt("AskTicks", 0);
            var offers = new ListTag();
            for (var offer : ConfessionMemory.offers(p, chapter)) offers.add(offer.save());
            own.put("Offers", offers);
            visitor.say(question);
            offer(p, own, chapter);
            if (!own.getBoolean("AnswerHint")) {
                own.putBoolean("AnswerHint", true);
                p.displayClientMessage(Component.literal("Choose an answer, or type your own in chat. Only he will hear it.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            }
            return false;
        }
        if (!own.contains("Answer")) {
            // The panel was closed by something else, or the server restarted: he asks again where he was.
            if (!Talk.waiting(p, key(chapter)) && !Talk.key(p).equals(key(chapter))) offer(p, own, chapter);
            int waited = own.getInt("AskTicks") + 15;
            own.putInt("AskTicks", waited);
            if (waited < SILENCE_TICKS) return false;
            own.putString("Answer", SILENCE);
            own.putString("Correction", "");
            if (!Talk.key(p).equals(key(chapter))) Talk.begin(p, key(chapter), ConfessionMemory.LISTENER);
            Talk.line(p, "", "He waits a while longer. Then he writes down that you did not answer.");
        }
        String answer = own.getString("Answer");
        String correction = answer.equals(SILENCE) ? "" : own.getString("Correction");
        // The question leaves his name tag as he writes; then a short rest before he goes on.
        String written = answer.equals(SILENCE) ? "" : fragment(answer);
        visitor.say(written.isEmpty() ? "…" : "…" + written + "…");
        if (Talk.key(p).equals(key(chapter))) {
            Talk.line(p, ConfessionMemory.LISTENER, written.isEmpty() ? "…" : "…" + written + "…");
            if (!correction.isEmpty()) Talk.line(p, "", "Under your answer, in the book, a line appears in another hand: \u201c" + correction + "\u201d");
            Talk.end(p);
        }
        own.putInt("RestTicks", REST_TICKS);
        own.putString("JournalText", own.getString("JournalText") + "\n\n“" + question + "”\n" + answer + (correction.isEmpty() ? "" : "\n" + ConfessionMemory.under(correction)) + "\n\n");
        var answers = own.getList("Answers", Tag.TAG_STRING);
        var corrections = own.getList("Corrections", Tag.TAG_STRING);
        while (answers.size() < chapter) answers.add(StringTag.valueOf(SILENCE));
        while (corrections.size() < answers.size()) corrections.add(StringTag.valueOf(""));
        if (answers.size() == chapter) { answers.add(StringTag.valueOf(answer)); corrections.add(StringTag.valueOf(correction)); }
        own.put("Answers", answers);
        own.put("Corrections", corrections);
        own.remove("Asked");
        own.remove("Answer");
        own.remove("Correction");
        own.remove("AskTicks");
        own.remove("Offers");
        return true;
    }

    /** The sealed journal's pages: what the reader wrote before, then each question and their answer, then the count. */
    public static List<String> pages(CompoundTag own, String counts, String name) {
        var pages = new ArrayList<String>();
        if (!own.getString("InitialWriting").isBlank()) pages.add(own.getString("InitialWriting"));
        var answers = own.getList("Answers", Tag.TAG_STRING);
        var corrections = own.getList("Corrections", Tag.TAG_STRING);
        for (int i = 0; i < QUESTIONS.length; i++) {
            String correction = i < corrections.size() ? corrections.getString(i) : "";
            pages.add("“" + QUESTIONS[i] + "”\n\n" + (i < answers.size() ? answers.getString(i) : SILENCE) + (correction.isEmpty() ? "" : "\n\n" + ConfessionMemory.under(correction)));
        }
        pages.add(counts + "\n\nSigned: " + name);
        return pages;
    }

    /** The reader's last answer, for the account; empty when they gave none. */
    public static String last(CompoundTag own) {
        var answers = own.getList("Answers", Tag.TAG_STRING);
        return answers.isEmpty() ? "" : answers.getString(answers.size() - 1);
    }

    /** How many of the reader's answers the House wrote under (0.4.275). */
    public static int corrected(CompoundTag own) {
        var corrections = own.getList("Corrections", Tag.TAG_STRING);
        int n = 0;
        for (int i = 0; i < corrections.size(); i++) if (!corrections.getString(i).isEmpty()) n++;
        return n;
    }

    /** The few words he repeats while he writes: the longest word of the answer and the two after it. */
    public static String fragment(String answer) {
        var words = new ArrayList<String>();
        for (String w : answer.split("\\s+")) { String t = w.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "").toLowerCase(java.util.Locale.ROOT); if (!t.isEmpty()) words.add(t); }
        if (words.isEmpty()) return "";
        int at = 0;
        for (int i = 1; i < words.size(); i++) if (words.get(i).length() > words.get(at).length()) at = i;
        return String.join(" ", words.subList(at, Math.min(words.size(), at + 3)));
    }

    static String clean(String raw) {
        String text = raw.replaceAll("\\p{Cntrl}", "").replaceAll("\\s+", " ").trim();
        return text.length() > LONGEST ? text.substring(0, LONGEST).trim() : text;
    }
}
