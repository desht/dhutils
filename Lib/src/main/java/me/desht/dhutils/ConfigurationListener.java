package me.desht.dhutils;

public interface ConfigurationListener {
//	public void onConfigurationValidate(ConfigurationManager configurationManager, String key, String val);
//	public void onConfigurationValidate(ConfigurationManager configurationManager, String key, List<?> val);
	public void onConfigurationValidate(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);
	public void onConfigurationChanged(ConfigurationManager configurationManager, String key, Object oldVal, Object newVal);
}
