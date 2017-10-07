package host.serenity.serenity.api.module

enum class ModuleCategory(val humanizedName: String, val displayInGui: Boolean) {
    COMBAT("Combat", true),
    MINIGAMES("Minigames", true),
    MISCELLANEOUS("Miscellaneous", true),
    MOVEMENT("Movement", true),
    OVERLAY("Overlay", false),
    PLAYER("Player", true),
    RENDER("Render", true),
    TWEAKS("Tweaks", false),
    WORLD("World", true)
}