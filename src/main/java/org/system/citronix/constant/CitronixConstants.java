package org.system.citronix.constant;

public final class CitronixConstants {
    // Tree-related constants
    public static final double YOUNG_TREE_PRODUCTIVITY = 2.5;  // kg per season
    public static final double MATURE_TREE_PRODUCTIVITY = 12.0; // kg per season
    public static final double OLD_TREE_PRODUCTIVITY = 20.0;    // kg per season

    public static final int YOUNG_TREE_AGE_LIMIT = 3;     // years
    public static final int MATURE_TREE_AGE_LIMIT = 10;   // years
    public static final int MAX_TREE_AGE = 20;            // years

    // Field-related constants
    public static final double MIN_FIELD_AREA = 0.1;      // hectares
    public static final double MAX_FIELD_PERCENTAGE = 0.5; // 50% of farm area
    public static final int MAX_TREES_PER_HECTARE = 100;
    public static final int MAX_FIELDS_PER_FARM = 10;

    // Planting period constants
    public static final int PLANTING_START_MONTH = 3;     // March
    public static final int PLANTING_END_MONTH = 5;       // May

    private CitronixConstants() {
        throw new IllegalStateException("Constants class");
    }
}