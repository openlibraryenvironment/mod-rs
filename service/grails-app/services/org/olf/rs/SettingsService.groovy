package org.olf.rs;

import org.olf.rs.settings.ISettings;

import com.k_int.web.toolkit.settings.AppSetting;

public class SettingsService implements ISettings {

	/**
	 * Returns the value for a setting
	 * @param setting the setting you want the value for
	 * @return the value for the setting, if it is not set then the default value will be returned
	 */
	public String getSettingValue(String setting) {
		String result = null;

		// Look up the setting

		if (setting) {
			AppSetting appSetting = AppSetting.findByKey(setting);
			if (appSetting != null) {
				result = appSetting.value;
				if (result == null) {
					// Take the default value
					result = appSetting.defValue;
				}
			}
		}

		// Return the result
		return(result);
	}

	/**
	 * Checks to see if the setting has the supplied value
	 * @param setting the setting that is to be checked
	 * @param value the value that it is compared against
	 * @return true if they match, otherwise false
	 */
	public boolean hasSettingValue(String setting, String value) {
		boolean result = false;

		String settingValue = getSettingValue(setting);

		if (settingValue == null) {
			// They must both be null
			result = (value == null);
		} else if (value != null) {
			// They must have the same value
			result = (settingValue == value);
		}

		// Return the result
		return(result);
	}

	/**
	 * Retrieves a settings as an integer
	 * @param setting the setting to be retrieved
	 * @param defaultValue if the value is null, the default value to be returned (default: 0)
	 * @param allowNegative If the value is false and the value is less than 0 then the default value is returned (default: false)
	 * @return The determined value, either from the setting or the default value
	 */
	public int getSettingAsInt(String setting, int defaultValue = 0, boolean allowNegative = false) {
		int value = defaultValue;
		String settingString = getSettingValue(setting);
		if (settingString != null) {
			try {
				// Now turn the string into an integer
				value = settingString.toInteger();

                // do we allow negative numbers
                if (!allowNegative && (value < 0)) {
                    // The value is negative, so reset to the default
                    value = defaultValue;
                }
			} catch (Exception e) {
				log.error("Unable to convert setting " + setting + " with value: " + settingString + " into an integer");
			}
		}

		return(value);
	}
}
