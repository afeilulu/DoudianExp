package cn.com.xinli.android.doudian.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {

	public static String ToDBC(String input) {
		if (input == null)
			return "";
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	public static String ToBC(String input) {
		if (input == null)
			return "";
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 32) {
				c[i] = (char) 12288;
			}
			if (c[i] > 32 && c[i] < 127) {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	public static String export(String input) {
		if (input == null)
			return "";
		StringBuffer sb = new StringBuffer(input);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c < 48) {
				sb.deleteCharAt(i--);
			} else if (c > 57 && c < 65) {
				sb.deleteCharAt(i--);
			} else if (c > 90 && c < 97) {
				sb.deleteCharAt(i--);
			} else if (c > 122 && c < 128) {
				sb.deleteCharAt(i--);
			} else if (c > 8207 && c < 8232) {
				sb.deleteCharAt(i--);
			} else if (c > 12288 && c < 12444) {
				sb.deleteCharAt(i--);
			} else if (c > 65280 && c < 65296) {
				sb.deleteCharAt(i--);
			} else if (c > 65305 && c < 65313) {
				sb.deleteCharAt(i--);
			} else if (c > 65338 && c < 65345) {
				sb.deleteCharAt(i--);
			} else if (c > 65370 && c < 65382) {
				sb.deleteCharAt(i--);
			} else if (c == 65509) {
				sb.deleteCharAt(i--);
			}
		}
		return sb.toString();
	}

	public static boolean isNumberOr_Letter(String str) {
		Matcher m = Pattern.compile("^[0-9a-zA-Z]+$").matcher(str);
		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static String L2S(List<String> list, String split) {
		if (list == null || list.size() < 1) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (String item : list) {
			sb.append(item).append(split);
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}
