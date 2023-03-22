package org.olf.rs.settings;

/**
 * Provides a very basic implemention of the ISettings interface
 *
 * @author Chas
 *
 */
public class MapSettings implements ISettings {

    private Map values = [ : ];

    /**
     * Adds a key / value pair into our values
     * @param key The key that is to be set
     * @param value The value to the set the key to
     */
    public void add(String key, String value) {
        // Just set the key with the value
        values[key] = value;
    }

    @Override
    public String getSettingValue(String setting) {
        String result = null;
        if (setting != null) {
            result = values[setting];
        }
        return(result);
    }

    @Override
    public boolean hasSettingValue(String setting, String value) {
        boolean result = false;
        if (setting != null) {
            String internalValue = values[setting];
            if (internalValue == null) {
                result = (value == null);
            } else if (value != null) {
                result = internalValue.equals(value);
            }
        }

        // Return the result
        return(result);
    }

    @Override
    public int getSettingAsInt(String setting, int defaultValue, boolean allowNegative) {
        int result = defaultValue;
        String settingString = getSettingValue(setting);
        if (settingString != null) {
            try {
                // Now turn the string into an integer
                result = settingString.toInteger();

                // do we allow negative numbers
                if (!allowNegative && (result < 0)) {
                    // The value is negative, so reset to the default
                    result = defaultValue;
                }
            } catch (Exception e) {
                // Unable to convert, so just ignore and take the default
            }
        }

        return(result);
    }
}
