package datatrackerstandards.settings;

public enum SettingType {
	INT(Integer.class),
	LONG(Long.class),
	BOOLEAN(Boolean.class),
	STRING(String.class),
	;

	private final Class<?> clazz;

	private SettingType(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Class<?> getClazz() {
		return clazz;
	}
}
