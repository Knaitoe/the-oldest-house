package io.github.knaitoe.theoldesthouse.house;

public enum HouseTransitionKind {
    DOOR,
    WINDOW,
    BREACH;

    public static HouseTransitionKind fromId(int id) {
        HouseTransitionKind[] values = values();
        if (id < 0 || id >= values.length) {
            return BREACH;
        }
        return values[id];
    }
}
