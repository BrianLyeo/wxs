package wxs.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import wxs.entities.CommonEntity;
import wxs.entities.EntityConverter;

public class WeiXinService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(WeiXinService.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String echostr = req.getParameter("echostr");

		if (echostr != null) {
			resp.getWriter().print(req.getParameter("echostr"));
		} else {
			logger.warn("no echostr");
		}
	}

//	@Override
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		byte[] buffer = new byte[4096];
//		InputStream input = req.getInputStream();
//		int num;
//		File f = new File("/opt/log/file.xml");
//		OutputStream output = new FileOutputStream(f);
//		
//		while ((num = input.read(buffer)) > 0) {
//			output.write(buffer, 0, num);
//		}
//		output.close();
//	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringWriter headerSW = new StringWriter();
		
		headerSW.append("\n");
		Enumeration<String> names = (Enumeration<String>)req.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			headerSW.append("[" + name + "] : [" + req.getHeader(name) + "]\n");
		}
		
		try {
			Reader bodyReader = new InputStreamReader(req.getInputStream(), "UTF-8");
			CommonEntity entity = EntityConverter.unpackXml(bodyReader, CommonEntity.class);
			CommonEntity repley = new CommonEntity();
			repley.setToUserName(entity.getFromUserName());
			repley.setFromUserName(entity.getToUserName());
			repley.setCreateTime(System.currentTimeMillis() / 1000);
			repley.setMsgType("text");
			repley.setContent("你刚才说“" + entity.getContent() + "”");
			
			Writer bodyWriter = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
			EntityConverter.packXml(repley, bodyWriter);
			bodyWriter.flush();
		} catch (Exception e) {
		}
		
		logger.info("headers: " + headerSW.toString());
	}

}

