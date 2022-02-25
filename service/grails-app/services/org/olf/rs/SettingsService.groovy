package org.olf.rs;

import com.k_int.web.toolkit.settings.AppSetting;

public class SettingsService {

	/**
	 * Returns the value for a setting
	 * @param setting the setting you want the value for
	 * @return the value for the setting, if it is not set then the default value will be returned
	 */
	public String getSettingValue(String setting) {
		String result = null;

		// Look up the setting
		AppSetting appSetting = AppSetting.findByKey(setting);
		if (appSetting != null) {
			result = appSetting.value;
			if (result == null) {
				// Take the default value
				result = appSetting.defValue;
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
		} else {
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
	 * @return The determined value, either from the setting or the default value
	 */
	public int getSettingAsInt(String setting, int defaultValue = 0) {
		int value = defaultValue;
		String settingString = getSettingValue(setting);
		if (settingString != null) {
			try {
				// Now turn the string into an integer
				value = settingString.toInteger();
			} catch (Exception e) {
				log.error("Unable to convert setting " + setting + " with value: " + settingString + " into an integer");
			}
		}
		
		return(value);
	}
}
