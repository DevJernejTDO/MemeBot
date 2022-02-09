package eu.triler.MemeBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigYML {

	private String path;
	private List<LineData> data = new ArrayList<LineData>();//lines
	
	/**
	 * Creates a configuration from a given file.
	 * @param file - Configuration file.
	 */
	public ConfigYML(File file) {
		SetUp(file);
	}
	
	/**
	 * Creates a default configuration from a root folder. File name config.yml.
	 */
	public ConfigYML() {
		SetUp(new File("config.yml"));
	}
	
	/**
	 * Creates a configuration from a given file.
	 * @param filePath - Configuration file path.
	 */
	public ConfigYML(String filePath) {
		SetUp(new File(filePath));
	}

	private void SetUp(File file) {
		path = file.getAbsolutePath();
		if(file.exists() && file.isFile()) {
			ReadFile(file);
		}
	}
	
	private void ReadFile(File file) {
		try {
			List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
			boolean isDataMultiline = false;
			int lastLine = -1;
			List<String> pathNdes = new ArrayList<String>();
			List<String> lineData = new ArrayList<String>();
			for(String line : lines) {
				int indexOfDataStart = GetIndexOfStartData(line);
				int index = indexOfDataStart/3;
				String onlyData = "";
				if(indexOfDataStart != -1) {
					onlyData = line.substring(indexOfDataStart);
				}
				boolean startsMultidata = onlyData.startsWith("-");
				if(isDataMultiline && startsMultidata) {
					String l = onlyData.substring(2);
					if(l.startsWith("\"") && l.endsWith("\"") || l.startsWith("'") && l.endsWith("'")) {
						l = l.substring(1, l.length() - 1);
					}
					lineData.add(l);
				} else {
					if(isDataMultiline && !startsMultidata) {
						SetList(ConstructPath(pathNdes), lineData);
						isDataMultiline = false;
					}
					if(line.isEmpty()) {
						WriteComment(null);
					} else if(line.startsWith("#") || line.trim().length() == 0) {
						WriteComment(line.substring(1));
					} else if(startsMultidata) {
						lineData.clear();
						String l = onlyData.substring(2);
						if(l.startsWith("\"") && l.endsWith("\"") || l.startsWith("'") && l.endsWith("'")) {
							l = l.substring(1, l.length() - 1);
						}
						lineData.add(l);
						isDataMultiline = true;
					} else {
						for(int i = index; i <= lastLine; i++) {
							pathNdes.remove(pathNdes.size() - 1);
						}
						int indexName = onlyData.indexOf(':');
						String name = onlyData.substring(0, indexName).replaceAll("'", "").replaceAll("\"", "");
						String value = onlyData.substring(indexName + 1);
						pathNdes.add(name);
						if(!value.equals("")) {
							value = value.substring(1);
							if(value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
								value = value.substring(1, value.length() - 1);
							}
							SetString(ConstructPath(pathNdes), value);
						}
						lastLine = index;
					}
				}
			}
			if(isDataMultiline) {
				SetList(ConstructPath(pathNdes), lineData);
				isDataMultiline = false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String ConstructPath(List<String> pathNdes) {
		String path = "";
		for(String name : pathNdes) {
			if(!path.isEmpty()) {
				path += ".";
			}
			path += name;
		}
		return path;
	}

	private int GetIndexOfStartData(String string) {
		char[] c = string.toCharArray();
		for(int i = 0; i < string.length(); i++) {
			if(c[i] != ' ') {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Saves default configuration.
	 */
	public void CopyDefault() {
		CopyDefault("config.yml");
	}
	
	/**
	 * Saves default configuration named *name.yml* from the project file.
	 * @param name - the name of the configuration.
	 */
	public void CopyDefault(String name) {
		File file = new File(path);
		if(file.exists()) {
			return;
		}
		InputStream in = getClass().getResourceAsStream("/" + name);
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader reader = new BufferedReader(is);
		try {
			path = path.replace('\\', '/');
			int index = path.lastIndexOf('/');
			File dir = new File(path.substring(0, (index == -1)?(0):(index)));
			dir.mkdirs();

			String line;
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
			while ((line = reader.readLine()) != null) {
		        writer.write(line);
		        writer.newLine();
		    }
			writer.close();
			ReadFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a file.
	 * @return - Returns the configuration file.
	 */
	public File GetFile() {
		if(this.path == null) {
			return null;
		}
		return new File(this.path);
	}
	
	/**
	 * Clears all the comments from the configuration.
	 */
	public void ClearComments() {
		data.removeIf(ld -> (ld.GetName() == null || ld.GetName().equals("")));
	}
	
	/**
	 * Adds a comment to the end of the configuration.
	 * @param comment - Text to be added.
	 */
	public void WriteComment(String comment) {
		WriteComment(-1, comment);
	}
	
	/**
	 * Adds a comment to the set line of the configuration.
	 * @param line - Line to add this comment on.
	 * @param comment - Text to be added.
	 */
	public void WriteComment(int line, String comment) {
		if(line == -1) {
			data.add(new LineData("", comment));
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			data.add(line, new LineData("", comment));
		}
	}
	
	/**
	 * Sets boolean data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - Boolean (data) to set.
	 */
	public void SetBoolean(String path, boolean value) {
		SetBoolean(-1, path, value);
	}
	
	/**
	 * Sets boolean data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - Boolean (data) to set.
	 */
	public void SetBoolean(int line, String path, boolean value) {
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	/**
	 * Sets string data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - String (data) to set.
	 */
	public void SetString(String path, String value) {
		SetString(-1, path, value);
	}
	
	/**
	 * Sets string data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - String (data) to set.
	 */
	public void SetString(int line, String path, String value) {
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	/**
	 * Sets integer data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - Integer (data) to set.
	 */
	public void SetInt(String path, int value) {
		SetInt(-1, path, value);
	}
	
	/**
	 * Sets integer data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - Integer (data) to set.
	 */
	public void SetInt(int line, String path, int value) {
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	/**
	 * Sets long data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - Long (data) to set.
	 */
	public void SetLong(String path, long value) {
		SetLong(-1, path, value);
	}
	
	/**
	 * Sets long data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - Long (data) to set.
	 */
	public void SetLong(int line, String path, long value) {
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	/**
	 * Sets double data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - Double (data) to set.
	 */
	public void SetDouble(String path, double value) {
		SetDouble(-1, path, value);
	}
	
	/**
	 * Sets double data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - Double (data) to set.
	 */
	public void SetDouble(int line, String path, double value) {
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	/**
	 * Sets double data to the end of the configuration.
	 * @param path - Path/node where to set the data.
	 * @param value - Double (data) to set.
	 */
	public void SetList(String path, List<?> value) {
		SetList(-1, path, value);
	}
	
	/**
	 * Sets double data to the end of the configuration.
	 * @param line - Line to add this comment on.
	 * @param path - Path/node where to set the data.
	 * @param value - Double (data) to set.
	 */
	public void SetList(int line, String path, List<?> value) {
		if(value == null || value.size() < 0) {
			return;
		}
		int index = FindKey(path);
		if(line <= -1) {
			if(index != -1) {
				data.get(index).Update(path, value);
			} else {
				LineData ld = new LineData(path, value);
				data.add(ld);
			}
		} else {
			line = Math.max(line, 0);
			line = Math.min(line, data.size());
			LineData ld;
			if(index != -1) {
				ld = data.remove(index);
				ld.Update(path, value);
			} else {
				ld = new LineData(path, value);
			}
			data.add(line, ld);
		}
	}
	
	private int FindKey(String path) {
		String key = GetKey(path);
		for(int i = 0; i < data.size(); i++) {
			String name = data.get(i).GetName();
			if(name != null && name.equals(key)) {
				return i;
			}
		}
		return -1;
	}
	
	private String GetKey(String path) {
		int index = path.indexOf('.');
		if(index == -1) {
			return path;
		}
		return path.substring(0, index);
	}
	
	/**
	 * Gets the keys of the given path.
	 * @param path - The sub-path where to check.
	 * @return Returns the array of keys.
	 */
	public List<String> GetKeys(String path) {
		if(path.equals("")) {
			List<String> keys = new ArrayList<String>();
			for(LineData ld : data) {
				String name = ld.GetName();
				if(name != null) {
					keys.add(name);
				}
			}
			return keys;
		}
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int indexNext = path.indexOf('.');
			if(indexNext == -1) {
				return ld.GetKeys("");
			}
			return ld.GetKeys(path.substring(indexNext + 1));
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Saves the configuration to the file.
	 */
	public void Save() {
		List<String> realLines = new ArrayList<String>();
		for(LineData ld : data) {
			ld.WriteLine(0, realLines);
		}
		try {
			File file = new File(path);
			path = path.replace('\\', '/');
			int index = path.lastIndexOf('/');
			File dir = new File(path.substring(0, (index == -1)?(0):(index)));
			dir.mkdirs();
			Files.write(file.toPath(), realLines, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Changes the path of the file configuration.
	 * @param path - New file path.
	 */
	public void ChangePath(String path) {
		this.path = path;
	}
	
	/**
	 * Prints the configuration to the console.
	 */
	public void Print() {
		for(LineData ld : data) {
			ld.PrintLine(0);
		}
	}
	
	/**
	 * Gets a boolean value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the boolean value.
	 */
	public boolean GetBoolean(String path) {
		return GetBoolean(path, false);
	}

	/**
	 * Gets a boolean value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the boolean value.
	 */
	public boolean GetBoolean(String path, boolean defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof String) {
				return ((String) obj).equals("true");
			} else if(obj instanceof Integer) {
				return ((int)obj) > 0;
			} else if(obj instanceof Double) {
				return ((double)obj) > 0;
			} else if(obj instanceof Long) {
				return ((long)obj) > 0;
			}
		}
		return defaultValue;
	}

	/**
	 * Gets a string value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the string value.
	 */
	public String GetString(String path) {
		return GetString(path, null);
	}

	/**
	 * Gets a string value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the string value.
	 */
	public String GetString(String path, String defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof String) {
				return (String) obj;
			} else if(obj instanceof Integer) {
				return (String) ("" + (int)obj);
			} else if(obj instanceof Double) {
				return (String) ("" + (double)obj);
			} else if(obj instanceof Long) {
				return (String) ("" + (long)obj);
			}
		}
		return defaultValue;
	}
	
	/**
	 * Gets a double value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the double value.
	 */
	public double GetDouble(String path) {
		return GetDouble(path, 0);
	}
	
	/**
	 * Gets a double value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the double value.
	 */
	public double GetDouble(String path, double defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof String) {
				String string = (String) obj;
				if(IsDouble(string)) {
					return Double.parseDouble(string);
				}
			} else if(obj instanceof Integer) {
				return ((Integer) obj).doubleValue();
			} else if(obj instanceof Double) {
				return (Double) obj;
			} else if(obj instanceof Long) {
				return ((Long) obj).doubleValue();
			}
		}
		return defaultValue;
	}
	
	/**
	 * Gets a long value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the long value.
	 */
	public long GetLong(String path) {
		return GetLong(path, 0);
	}
	
	/**
	 * Gets a long value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the long value.
	 */
	public long GetLong(String path, long defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof String) {
				String string = (String) obj;
				if(IsLong(string)) {
					return Long.parseLong(string);
				}
			} else if(obj instanceof Integer) {
				return ((Integer) obj).longValue();
			} else if(obj instanceof Double) {
				return ((Double) obj).longValue();
			} else if(obj instanceof Long) {
				return (Long) obj;
			}
		}
		return defaultValue;
	}
	
	/**
	 * Gets a integer value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the integer value.
	 */
	public int GetInt(String path) {
		return GetInt(path, 0);
	}
	
	/**
	 * Gets a integer value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the integer value.
	 */
	public int GetInt(String path, int defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof String) {
				String string = (String) obj;
				if(IsInteger(string)) {
					return Integer.parseInt(string);
				}
			} else if(obj instanceof Integer) {
				return (Integer) obj;
			} else if(obj instanceof Double) {
				return ((Double) obj).intValue();
			} else if(obj instanceof Long) {
				return ((Long) obj).intValue();
			}
		}
		return defaultValue;
	}
	
	/**
	 * Gets a list value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the list value.
	 */
	public List<?> GetList(String path) {
		return GetList(path, null);
	}
	
	/**
	 * Gets a list value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the list value.
	 */
	public List<?> GetList(String path, List<?> defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof List<?>) {
				List<?> list = (List<?>) obj;
				return list;
			}
		}
		return defaultValue;
	}
	
	/**
	 * Gets a string list value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @return Returns the string list value.
	 */
	public List<String> GetStringList(String path) {
		return GetStringList(path, null);
	}
	
	/**
	 * Gets a string list value from the configuration.
	 * @param path - Path/node where to set the data.
	 * @param defaultValue - Default value if not set.
	 * @return Returns the string list value.
	 */
	public List<String> GetStringList(String path, List<String> defaultValue) {
		int index = FindKey(path);
		if(index != -1) {
			LineData ld = data.get(index);
			int i = path.indexOf('.');
			Object obj = null;
			if(i != -1) {
				String s = path.substring(i + 1);
				obj = ld.GetData(s);
			} else {
				obj = ld.GetData("");
			}
			if(obj instanceof List<?>) {
				List<?> list = (List<?>) obj;
				List<String> stringList = new ArrayList<String>();
				for(Object o : list) {
					String str = (String) o;
					if(str.startsWith("'") && str.endsWith("'") || str.startsWith("\"") && str.endsWith("\"")) {
						str = str.substring(1, str.length() - 1);
					}
					stringList.add(str);
				}
				return stringList;
			}
		}
		return defaultValue;
	}


	private boolean IsDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch(Exception e) {}
		return false;
	}

	private boolean IsLong(String string) {
		try {
			Long.parseLong(string);
			return true;
		} catch(Exception e) {}
		return false;
	}

	private boolean IsInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch(Exception e) {}
		return false;
	}
	
}


class LineData {
	
	private String name;
	private Map<String, LineData> subLines = new HashMap<String, LineData>();
	private Object data;
	
	public LineData(String path, Object value) {
		Update(path, value);
	}

	public List<String> GetKeys(String path) {
		if(path.equals("")) {
			return new ArrayList<String>(subLines.keySet());
		}
		int index = path.indexOf('.');
		if(index == -1) {
			LineData ld = subLines.get(path);
			return ld.GetKeys("");
		}
		String pathName = path.substring(0, index);
		String pathNext = path.substring(index + 1);
		LineData ld = subLines.get(pathName);
		if(ld == null) {
			return new ArrayList<String>();
		}
		return ld.GetKeys(pathNext);
	}

	public void Update(String path, Object value) {
		if(path.equals("")) {
			this.data = value;
			this.name = null;
		} else {
			int index = path.indexOf('.');
			if(index == -1) {
				this.name = path;
				this.data = value;
				subLines.clear();
			} else {
				this.data = null;
				this.name = path.substring(0, index);
				String newPath = path.substring(index + 1);
				int newIndex = newPath.indexOf('.');
				String newName = newPath;
				if(newIndex != -1) {
					newName = newPath.substring(0, newIndex);
				}
				LineData ld = subLines.get(newName);
				if(ld == null) {
					ld = new LineData(newPath, value);
					subLines.put(newName, ld);
				} else {
					ld.Update(newPath, value);
				}
			}
		}
	}

	public Object GetData(String path) {
		if(path.equals("")) {
			return this.data;
		} else {
			int index = path.indexOf('.');
			if(index == -1) {
				LineData ld = this.subLines.get(path);
				if(ld == null) {
					return null;
				}
				return ld.GetData("");
			} else {
				String pPath = path.substring(0, index);
				LineData ld = this.subLines.get(pPath);
				if(ld == null) {
					return null;
				}
				String newPath = path.substring(index + 1);
				return ld.GetData(newPath);
			}
		}
	}
	
	public void PrintLine(int i) {
		if(this.name == null) {
			if(this.data == null) {
				System.out.println();
			} else {
				System.out.println("#" + this.data);
			}
		} else {
			String line = GetSpacing(i);
			if(IsNumber(name)) {
				line += "'" + name + "':";
			} else {
				line += name + ":";
			}
			if(this.data != null && !(this.data instanceof ArrayList)) {
				line += " " + this.data;
			}
			System.out.println(line);
			if(this.data instanceof ArrayList) {
				String spacing = GetSpacing(i);
				for(Object li : ((ArrayList<?>) this.data)) {
					System.out.println(spacing + "- " + li);
				}
			}
			for(LineData ld : subLines.values()) {
				ld.PrintLine(i + 1);
			}
		}
	}

	public void WriteLine(int i, List<String> lines) {
		if(this.name == null) {
			if(this.data == null) {
				lines.add("");
			} else {
				lines.add("#" + this.data);
			}
		} else {
			String line = GetSpacing(i);
			if(IsNumber(name)) {
				line += "'" + name + "':";
			} else {
				line += name + ":";
			}
			if(this.data != null && !(this.data instanceof ArrayList)) {
				line += " " + this.data;
				
			}
			lines.add(line);
			if(this.data instanceof ArrayList) {
				String spacing = GetSpacing(i);
				for(Object li : ((ArrayList<?>) this.data)) {
					lines.add(spacing + "- " + li);
				}
			}
			for(LineData ld : subLines.values()) {
				ld.WriteLine(i + 1, lines);
			}
		}
	}

	private boolean IsNumber(String number) {
		for(char c : number.toCharArray()) {
			if(c < 48 || c > 57) {
				return false;
			}
		}
		return true;
	}
	
	private String GetSpacing(int i) {
		String out = "";
		for(int q = 0; q < 3 * i; q++) {
			out += " ";
		}
		return out;
	}

	public String GetName() {
		return this.name;
	}
	
}
