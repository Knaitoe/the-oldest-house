package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;

public final class HouseTransitionContextState {
    private static HouseTransitionKind currentKind = HouseTransitionKind.DOOR;
    private static int currentToken = -1;
    private static boolean armed;

    private HouseTransitionContextState() {
    }

    public static synchronized void set(HouseTransitionKind kind, int token) {
        currentKind = kind == null ? HouseTransitionKind.BREACH : kind;
        currentToken = token;
        armed = true;
    }

    public static synchronized HouseTransitionKind peekKind() {
        return armed ? currentKind : HouseTransitionKind.DOOR;
    }

    public static synchronized int peekToken() {
        return currentToken;
    }

    public static synchronized boolean isArmed() {
        return armed;
    }

    public static synchronized void clear(int token) {
        if (!armed || currentToken != token) {
            return;
        }

        armed = false;
        currentKind = HouseTransitionKind.DOOR;
        currentToken = -1;
    }
}
