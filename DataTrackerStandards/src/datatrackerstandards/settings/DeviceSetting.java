package datatrackerstandards.settings;

import java.lang.reflect.Field;

import datatrackerserver.entities.Device;

public enum DeviceSetting implements Setting<Device> {
	QUOTA(SettingType.INT, 0),
	THRESHOLD(SettingType.INT, 95),
	AUTO_SHUTOFF(SettingType.BOOLEAN, true),
	;

	private final SettingType type;
	private final Object defaultValue;
	private final Field settingField;

	private DeviceSetting(SettingType type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;

		Field tempField = null;
		String fieldName;
		String[] nameWords = this.name().split("_");
		Field[] fields = Device.class.getDeclaredFields();
		for(Field field : fields) {
			fieldName = field.getName().toUpperCase();
			boolean match = true;
			for(String word : nameWords) {
                if(!fieldName.contains(word)) {
                	match = false;
                	break;
                }
			}
			if(match) {
				tempField = field;
				break;
			}
		}

		settingField = tempField;
	}

	public SettingType getType() {
		return type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Field getSettingField() {
		return settingField;
	}

	public void setValue(Device device, Object value) throws IllegalArgumentException, IllegalAccessException {
		settingField.set(device, value);
	}

	public void setDefaultValue(Device device) throws IllegalArgumentException, IllegalAccessException {
		settingField.set(device, defaultValue);
	}
}