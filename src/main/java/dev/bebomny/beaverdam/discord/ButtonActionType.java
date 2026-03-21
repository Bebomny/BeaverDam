package dev.bebomny.beaverdam.discord;

import lombok.Getter;
import net.dv8tion.jda.api.components.buttons.Button;


@Getter
public enum ButtonActionType {
    // Anime Item Buttons
    ANIME_ITEM_DOWNLOAD("BTN_AI_DOWNLOAD", "anime_rss_item", "Download", "primary"),
    ANIME_ITEM_SET_AS_INTERESTING("BTN_AI_SET_INTERESTING", "anime_rss_item", "Set As Interesting", "primary"),

    // New Show Series Buttons
    SHOW_SERIES_INTERESTING("BTN_SS_INTERESTING", "show_series", "Interesting", "primary"),
    SHOW_SERIES_IGNORED("BTN_SS_IGNORED", "show_series", "Ignored", "primary"),
    SHOW_SERIES_AUTODOWNLOAD("BTN_SS_AUTODOWNLOAD", "show_series", "AutoDownload", "primary"),
    SHOW_SERIES_SUBMIT("BTN_SS_SUBMIT", "show_series", "Submit", "success");

    private final String id;
    private final String itemType;
    private final String label;
    private final String buttonType;

    ButtonActionType(String id, String itemType, String label, String buttonType) {
        this.id = id;
        this.itemType = itemType;
        this.label = label;
        this.buttonType = buttonType;
    }

    public static ButtonActionType fromId(String id) {
        for (ButtonActionType actionId : ButtonActionType.values()) {
            if (actionId.id.equals(id)) {
                return actionId;
            }
        }
        throw new IllegalArgumentException("Unknown button action id " + id);
    }

    public Button createButton() {
        return switch (buttonType) {
            case "primary" -> Button.primary(id, label);
            case "secondary" -> Button.secondary(id, label);
            case "success" -> Button.success(id, label);
            default -> throw new IllegalArgumentException("Unknown button type " + buttonType);
        };
    }
}
