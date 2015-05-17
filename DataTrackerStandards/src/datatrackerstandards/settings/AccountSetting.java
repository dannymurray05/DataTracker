package datatrackerstandards.settings;

import java.lang.reflect.Field;

import datatrackerserver.entities.Account;

public enum AccountSetting implements Setting<Account> {
	QUOTA(SettingType.INT, 1 << 10),
	THRESHOLD(SettingType.INT, 95),
	BILLING_CYCLE(SettingType.INT, 1),
	;

	private final SettingType type;
	private final Object defaultValue;
	private final Field settingField;

	private AccountSetting(SettingType type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;

		Field tempField = null;
		String fieldName;
		String[] nameWords = this.name().split("_");
		Field[] fields = Account.class.getDeclaredFields();
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

	public void setValue(Account account, Object value) throws IllegalArgumentException, IllegalAccessException {
		settingField.set(account, value);
	}

	public void setDefaultValue(Account account) throws IllegalArgumentException, IllegalAccessException {
		settingField.set(account, defaultValue);
	}
}