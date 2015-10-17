package wxs.filter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class RequestFilter implements Filter {

	private static final Logger logger = Logger.getLogger(RequestFilter.class);
	private static Pattern queryPat = Pattern.compile("^([A-Za-z0-9_-]+)=([A-Za-z0-9_-]*)$");

	private String token;

	public void destroy() {

	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (logger.isDebugEnabled()) {
			logger.debug("Got Request : " + request.getMethod() + " " + request.getPathInfo());
		}

		Map<String, String> queries = parseQueryString(request.getQueryString());
		if (checkTimestamp(queries.get("timestamp"))
				&& checkSignature(queries.get("timestamp"), queries.get("nonce"), queries.get("signature"))) {
			chain.doFilter(request, response);
		} else {
			response.sendError(403);
		}
	}

	public void init(FilterConfig config) throws ServletException {
		token = config.getServletContext().getInitParameter("my-token");
		if (token == null) {
			token = "";
		}
	}

	private boolean checkTimestamp(String timestamp) {
		boolean ok = false;

		try {
			long time = Long.valueOf(timestamp);
			long dis = System.currentTimeMillis() / 1000 - time;
			ok = Math.abs(dis) <= 900;
		} catch (NumberFormatException e) {
		}

		return ok;
	}

	private boolean checkSignature(String timestamp, String nonce, String userSign) {
		boolean ok = false;

		if (timestamp == null || nonce == null || userSign == null) {
			return ok;
		}

		List<String> source = new ArrayList<String>();
		source.add(token);
		source.add(timestamp);
		source.add(nonce);
		Collections.sort(source);

		StringBuilder sb = new StringBuilder();
		for (String m : source) {
			sb.append(m);
		}
		byte[] tosign = sb.toString().getBytes();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(tosign);
			byte[] digest = md.digest();
			ok = Hex.encodeHexString(digest).equals(userSign);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("compute signature failed", e);
		}

		return ok;
	}

	private Map<String, String> parseQueryString(String query) {
		Map<String, String> res = new HashMap<String, String>();

		if (StringUtils.isBlank(query)) {
			return res;
		}

		String[] queries = StringUtils.split(query, '&');
		for (String q : queries) {
			Matcher matcher = queryPat.matcher(q);
			if (matcher.matches()) {
				res.put(matcher.group(1), matcher.group(2));
			}
		}

		return res;
	}

}
