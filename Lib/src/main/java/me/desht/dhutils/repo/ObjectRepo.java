package me.desht.dhutils.repo;

import java.util.HashMap;
import java.util.Map;

import me.desht.dhutils.DHUtilsException;

public class ObjectRepo {
	private Map<String, Map<String, DHStorable>> map = new HashMap<String, Map<String,DHStorable>>();
	
	public <T extends DHStorable> T get(Class<T> type, String name) {
		Map<String, DHStorable> m = map.get(type.getCanonicalName());
		
		if (m == null)
			throw new DHUtilsException("Unknown type " + type.getCanonicalName());
		
		return type.cast(map.get(type.getCanonicalName()).get(name));
	}
	
	public boolean check(Class<? extends DHStorable> type, String name) {
		Map<String, DHStorable> m = map.get(type.getCanonicalName());
		if (m == null || !m.containsKey(name))
			return false;
		else
			return true;
	}
	
	public  <T extends DHStorable> T create(Class<T> type, String name, Object... args) {
		Map<String, DHStorable> m = map.get(type.getCanonicalName());
		
		if (m == null) {
			m = new HashMap<String, DHStorable>();
			map.put(type.getCanonicalName(), m);
		}
		
		DHStorable obj = createObject(type, name, args);
		
		m.put(name, obj);
		return null;
		
	}

	private  <T extends DHStorable> T createObject(Class<T> type, String name, Object... args) {
		// TODO Auto-generated method stub
		
		// see if the class has a constructor which takes a String (the name) as the first arg
		
		return null;
	}
}
