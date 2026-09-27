package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;

public final class HouseTransitionContextState {
    private static HouseTransitionKind nextKind = HouseTransitionKind.DOOR;

    private HouseTransitionContextState() {
    }

    public static synchronized void set(HouseTransitionKind kind) {
        nextKind = kind == null ? HouseTransitionKind.BREACH : kind;
    }

    public static synchronized HouseTransitionKind consume() {
        HouseTransitionKind result = nextKind;
        nextKind = HouseTransitionKind.DOOR;
        return result;
    }
}
