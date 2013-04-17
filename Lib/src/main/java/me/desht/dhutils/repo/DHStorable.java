package me.desht.dhutils.repo;

import java.io.File;
import java.util.Map;

public interface DHStorable {
	public String getName();
	public File getStorageFolder();
	public Map<String, Object> freeze();
}
