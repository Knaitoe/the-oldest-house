package io.github.knaitoe.theoldesthouse.gametest;

/**
 * The Witness pool every case pins, in one place, so a new story changes one line here rather than sixteen across the suite:
 * the sources in {@code WitnessAccount.Story}, the resolutions required (75%, rounded up), and the literary places among them.
 * 0.4.209: the closing hall makes 62 / 47 / 40; 0.4.210, the storeroom, 63 / 48 / 41; 0.4.211, the stillness, 64 / 48 / 42; 0.4.212, the line, 65 / 49 / 43; 0.4.213, your own light, 66 / 50 / 44; 0.4.215, the household (not a place), 67 / 51 / 44; 0.4.217, Polybius, 68 / 51 / 45; 0.4.218, the replacement, 69 / 52 / 46.
 */
final class WitnessPool {
    private WitnessPool(){}
    static final int SOURCES=69,REQUIRED=52,LITERARY=46;
}
