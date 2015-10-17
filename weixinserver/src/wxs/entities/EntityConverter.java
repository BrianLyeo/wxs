package wxs.entities;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class EntityConverter {
	public static <T> T unpackXml(String xml, Class<T> klass) throws Exception {
		return unpackXml(new StringReader(xml), klass);
	}

	@SuppressWarnings("unchecked")
	public static <T> T unpackXml(Reader reader, Class<T> klass) throws Exception {
		T entity = klass.newInstance();

		SAXReader sax = new SAXReader();
		Document document = sax.read(reader);
		Element root = document.getRootElement();
		Iterator<Element> iter = root.elementIterator();
		while (iter.hasNext()) {
			Element ele = iter.next();
			Method getMethod = klass.getMethod("get" + ele.getName());
			Class<?> filedType = getMethod.getReturnType();
			Method setMethod = klass.getMethod("set" + ele.getName(), filedType);
			String strValue = ele.getTextTrim();

			if (Long.class.equals(filedType) || Long.TYPE.equals(filedType)) {
				setMethod.invoke(entity, Long.valueOf(strValue));
			} else if (Integer.class.equals(filedType) || Integer.TYPE.equals(filedType)) {
				setMethod.invoke(entity, Integer.valueOf(strValue));
			} else if (Short.class.equals(filedType) || Short.TYPE.equals(filedType)) {
				setMethod.invoke(entity, Short.valueOf(strValue));
			} else if (String.class.equals(filedType)) {
				if (getMethod.getAnnotation(UnparsedCharacterData.class) != null) {
					setMethod.invoke(entity, parseCDataValue(strValue));
				} else {
					setMethod.invoke(entity, strValue);
				}
			} else {
				throw new Exception("unsupported filed type : " + filedType.getName());
			}
		}
		return entity;
	}

	public static void packXml(Object obj, Writer writer) throws Exception {
		Class<?> klass = obj.getClass();
		EntityRootElement root = klass.getAnnotation(EntityRootElement.class);
		String rootName = klass.getName();
		if (root != null && root.name() != null && !root.name().isEmpty()) {
			rootName = root.name();
		}
		
		writer.append("<" + rootName + ">\n");
		Method[] methods = klass.getMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			if (!methodName.startsWith("get")) {
				continue;
			}
			
			String name = methodName.substring(3);
			if (name.charAt(0) < 'A' || name.charAt(0) > 'Z' || name.equals("Class")) {
				continue;
			}
			Object val = method.invoke(obj);
			if (val == null) {
				continue;
			}
			String value = val.toString();
			if (method.getAnnotation(UnparsedCharacterData.class) != null) {
				value = "<![CDATA[" + value + "]]>";
			} else {
				value = StringUtils.replace(value, "&", "&amp;");
				value = StringUtils.replace(value, "<", "&lt;");
				value = StringUtils.replace(value, ">", "&gt;");
				value = StringUtils.replace(value, "'", "&apos;");
				value = StringUtils.replace(value, "\"", "&quot;");
			}
			
			writer.append("\t<" + name + ">");
			writer.append(value);
			writer.append("</" + name + ">\n");
		}
		writer.append("</" + rootName + ">\n");
	}

	// <![CDATA[oezHpwsdoSCHLLRsg19A3uIv47nA]]>
	private static Pattern cdatapat = Pattern.compile("^<!\\[CDATA\\[(.*)\\]\\]>$");

	private static String parseCDataValue(String source) {
		Matcher matcher = cdatapat.matcher(source);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return source;
	}
}
