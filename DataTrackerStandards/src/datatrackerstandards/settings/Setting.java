package datatrackerstandards.settings;

import java.lang.reflect.Field;

public interface Setting<T> {
	public SettingType getType();

	public Object getDefaultValue();

	public Field getSettingField();

	public void setValue(T settingObject, Object value) throws IllegalArgumentException, IllegalAccessException;

	public void setDefaultValue(T settingObject) throws IllegalArgumentException, IllegalAccessException;
}
