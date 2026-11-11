package io.github.knaitoe.theoldesthouse.labyrinth;

/**
 * 0.4.484: somebody, or something with somebody in it, whose line is shown as a caption (client.SpeechCaptions). The words are its
 * custom name while that is visible. A person is one (LiteraryActor), and so is a car whose driver calls out of it (TownCar).
 */
public interface Voiced {
    /** Who is saying the line that is up now, or empty. */
    String speaker();
}
