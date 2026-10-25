package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The listener's questions (0.4.121). After each chapter he has dictated, he asks the person holding the pen one question,
 * and the reader answers by typing in chat. The answer never reaches public chat: only the reader sees their own words and
 * the fragment he repeats while he writes it down. Thirty seconds of silence in front of him is written down as that. The
 * sealed journal is made of the questions and the reader's own answers.
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

    /** Whether this reader is in the middle of a chapter with him, so anything they say in chat is said to him alone. */
    public static boolean engaged(CompoundTag own) {
        return own.hasUUID("Journal") && own.getInt("Chapter") < LiteraryTexts.CONFESSION.length && own.getInt("ChapterVisit") != own.getInt("Visit");
    }

    /**
     * A chat line from a reader in the confession. Returns whether it was taken out of public chat: an answer to the open
     * question is written down and echoed back in fragment; anything said before he asks is held back too, and he says so.
     */
    public static boolean heard(ServerPlayer p, CompoundTag own, String raw) {
        if (!engaged(own)) return false;
        String answer = clean(raw);
        if (answer.isEmpty()) return true;
        p.displayClientMessage(Component.literal("You, quietly: “" + answer + "”").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        if (!own.getBoolean("Asked") || own.contains("Answer")) {
            p.displayClientMessage(Component.literal("The listener lifts a finger from the page: not yet."), false);
            return true;
        }
        own.putString("Answer", answer);
        p.displayClientMessage(Component.literal("The listener, writing: “…" + fragment(answer) + "…”"), false);
        return true;
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
            visitor.say(question);
            p.displayClientMessage(Component.literal("The listener: “" + question + "”"), false);
            if (!own.getBoolean("AnswerHint")) {
                own.putBoolean("AnswerHint", true);
                p.displayClientMessage(Component.literal("Answer him in chat. Only he will hear it.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            }
            return false;
        }
        if (!own.contains("Answer")) {
            int waited = own.getInt("AskTicks") + 15;
            own.putInt("AskTicks", waited);
            if (waited < SILENCE_TICKS) return false;
            own.putString("Answer", SILENCE);
            p.displayClientMessage(Component.literal("He waits a while longer. Then he writes down that you did not answer."), false);
        }
        String answer = own.getString("Answer");
        own.putString("JournalText", own.getString("JournalText") + "\n\n“" + question + "”\n" + answer + "\n\n");
        var answers = own.getList("Answers", Tag.TAG_STRING);
        while (answers.size() < chapter) answers.add(StringTag.valueOf(SILENCE));
        if (answers.size() == chapter) answers.add(StringTag.valueOf(answer));
        own.put("Answers", answers);
        own.remove("Asked");
        own.remove("Answer");
        own.remove("AskTicks");
        return true;
    }

    /** The sealed journal's pages: what the reader wrote before, then each question and their answer, then the count. */
    public static List<String> pages(CompoundTag own, String counts, String name) {
        var pages = new ArrayList<String>();
        if (!own.getString("InitialWriting").isBlank()) pages.add(own.getString("InitialWriting"));
        var answers = own.getList("Answers", Tag.TAG_STRING);
        for (int i = 0; i < QUESTIONS.length; i++)
            pages.add("“" + QUESTIONS[i] + "”\n\n" + (i < answers.size() ? answers.getString(i) : SILENCE));
        pages.add(counts + "\n\nSigned: " + name);
        return pages;
    }

    /** The reader's last answer, for the account; empty when they gave none. */
    public static String last(CompoundTag own) {
        var answers = own.getList("Answers", Tag.TAG_STRING);
        return answers.isEmpty() ? "" : answers.getString(answers.size() - 1);
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
