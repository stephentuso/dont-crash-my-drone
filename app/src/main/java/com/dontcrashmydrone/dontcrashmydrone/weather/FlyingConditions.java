package com.dontcrashmydrone.dontcrashmydrone.weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephentuso on 4/26/16.
 */
public class FlyingConditions {

    private List<String> warnings = new ArrayList<>();
    private int conditionInt = 0;
    private String conditionDescription = null;

    public static final int CONDITION_GOOD = 0;
    public static final int CONDITION_MEDIUM = 1;
    public static final int CONDITION_POOR = 2;

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public List<String> getWarnings() {
        return this.warnings;
    }

    public void setConditionInt(int conditionInt) {
        this.conditionInt = conditionInt;
    }

    public void incrementConditionIntBy(int amount) {
        this.conditionInt += amount;
    }

    public int getConditionInt() {
        return this.conditionInt;
    }

    public int getConditionCode() {
        if (this.conditionInt >= 4) {
            return CONDITION_POOR;
        } else if (this.conditionInt >= 2) {
            return CONDITION_MEDIUM;
        } else {
            return CONDITION_GOOD;
        }
    }

}