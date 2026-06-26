package com.valentin.tu_cv_spring_bot.TuCv.mODEL;

public enum Linea {
    // AVON
    TODAY_TOMORROW_ALWAYS("Today Tomorrow Always", "AVON", "PERFUME"),
    ATTRACTION("Attraction", "AVON", "PERFUME"),
    FAR_AWAY("Far Away", "AVON", "PERFUME"),
    IMARI("Imari", "AVON", "PERFUME"),
    SOFT_MUSK("Soft Musk", "AVON", "PERFUME"),
    SWEET_HONESTY("Sweet Honesty", "AVON", "PERFUME"),
    PASION_GITANA("Pasión Gitana", "AVON", "PERFUME"),
    MAXIME("Maxime", "AVON", "PERFUME"),
    LUCKY_FOR_ME("Lucky for Me", "AVON", "PERFUME"),
    LOV_U("Lov|U", "AVON", "PERFUME"),
    MESMERIZE("Mesmerize", "AVON", "PERFUME"),
    PRINCESA("Princesa", "AVON", "PERFUME"),

    ULTRA_COLOR("Ultra Color", "AVON", "MAQUILLAJE"),
    MARK("Mark", "AVON", "MAQUILLAJE"),
    COLOR_TREND("Color Trend", "AVON", "MAQUILLAJE"),
    EPIC_LIP("Epic Lip", "AVON", "MAQUILLAJE"),
    UNLIMITED("Unlimited", "AVON", "MAQUILLAJE"),
    AVON_COLOR("Avon Color", "AVON", "MAQUILLAJE"),
    TRUE_LINE("True", "AVON", "MAQUILLAJE"),

    LOTUS_SHIELD("Lotus Shield", "AVON", "CABELLO"),
    DISNEY("Disney", "AVON", "CABELLO"),
    AVON_NATURALS("Avon Naturals", "AVON", "CABELLO"),

    ANEW("Anew", "AVON", "CREMA"),
    AVON_CARE("Avon Care", "AVON", "CREMA"),
    SIMPLY_DELICATE("Simply Delicate", "AVON", "CREMA"),

    FOOT_WORKS("Foot Works", "AVON", "CUIDADO_DIARIO"),
    ENCANTO("Encanto", "AVON", "CUIDADO_DIARIO"),
    ON_DUTY("On Duty", "AVON", "CUIDADO_DIARIO"),
    CASA_ESTILO("Casa & Estilo", "AVON", "CUIDADO_DIARIO"),
    CARE_LINE("Care", "AVON", "CUIDADO_DIARIO"),

    // NATURA
    KAIAK("Kaiak", "NATURA", "PERFUME"),
    HUMOR("Humor", "NATURA", "PERFUME"),
    KRISKA("Kriska", "NATURA", "PERFUME"),
    ESSENCIAL("Essencial", "NATURA", "PERFUME"),
    NATURA_HOMEM("Natura Homem", "NATURA", "PERFUME"),
    LUNA("Luna", "NATURA", "PERFUME"),
    SR_N("Sr. N", "NATURA", "PERFUME"),
    SINTONIA("Sintonía", "NATURA", "PERFUME"),
    ILIA("Ilía", "NATURA", "PERFUME"),
    BIOGRAFIA("Biografía", "NATURA", "PERFUME"),
    AGUAS("Águas", "NATURA", "PERFUME"),

    UNA("Una", "NATURA", "MAQUILLAJE"),
    FACES("Faces", "NATURA", "MAQUILLAJE"),
    COLECCION_LIBRE("Colección Libre", "NATURA", "MAQUILLAJE"),

    LUMINA("Lumina", "NATURA", "CABELLO"),
    EKOS_CABELLO("Ekos Cabello", "NATURA", "CABELLO"),

    CHRONOS("Chronos", "NATURA", "CREMA"),
    TODODIA_CREMA("Tododia Crema", "NATURA", "CREMA"),
    EKOS_CREMA("Ekos Crema", "NATURA", "CREMA"),

    TODODIA_DIARIO("Tododia Corporal", "NATURA", "CUIDADO_DIARIO"),
    EKOS_DIARIO("Ekos Corporal", "NATURA", "CUIDADO_DIARIO"),
    FRESCOR("Frescor", "NATURA", "CUIDADO_DIARIO");

    private final String displayName;
    private final String category;
    private final String subCategory;

    Linea(String displayName, String category, String subCategory) {
        this.displayName = displayName;
        this.category = category;
        this.subCategory = subCategory;
    }

    public String getDisplayName() { return displayName; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }

    public static Linea fromDisplayName(String displayName) {
        for (Linea l : values()) {
            if (l.displayName.equalsIgnoreCase(displayName)) return l;
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Linea fromString(String s) {
        if (s == null || s.isBlank()) return null;
        for (Linea l : values()) {
            if (l.name().equalsIgnoreCase(s) || l.displayName.equalsIgnoreCase(s)) return l;
        }
        return null;
    }
}
