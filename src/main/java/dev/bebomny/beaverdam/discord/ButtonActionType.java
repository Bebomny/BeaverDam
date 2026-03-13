package dev.bebomny.beaverdam.discord;

import lombok.Getter;

@Getter
public enum ButtonActionType {
    DOWNLOAD("BTN_DOWNLOAD"),
    SET_AS_INTERESTING("BTN_SET_INTERESTING");

    private final String id;

    ButtonActionType(String id) {
        this.id = id;
    }

    public static ButtonActionType fromId(String id) {
        for (ButtonActionType actionId : ButtonActionType.values()) {
            if (actionId.id.equals(id)) {
                return actionId;
            }
        }
        throw new IllegalArgumentException("Unknown button action id " + id);
    }
}
