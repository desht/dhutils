package me.desht.dhutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.bukkit.plugin.Plugin;

public class JARUtil {
	public enum ExtractWhen { ALWAYS, IF_NOT_EXISTS, IF_NEWER }

	public static final Charset TARGET_ENCODING = Charset.forName("UTF-8");
	public static final Charset SOURCE_ENCODING = Charset.forName("UTF-8");

	private final Plugin plugin;

	public JARUtil(Plugin plugin) {
		this.plugin = plugin;
	}

	public void extractResource(String from, File to) {
		extractResource(from, to, ExtractWhen.IF_NEWER);
	}

	public void extractResource(String from, File to, ExtractWhen when) {
		File of = to;
		if (to.isDirectory()) {
			String fname = new File(from).getName();
			of = new File(to, fname);
		} else if (!of.isFile()) {
			LogUtils.warning("not a file: " + of);
			return;
		}

		File jarFile = getJarFile();

		LogUtils.finer("extractResource: file=" + of +
		               ", file-last-mod=" + of.lastModified() +
		               ", file-exists=" + of.exists() +
		               ", jar-last-mod=" +  jarFile.lastModified() +
		               ", when=" + when);

		// if the file exists and is newer than the JAR, then we'll leave it alone
		if (of.exists() && when == ExtractWhen.IF_NOT_EXISTS) {
			return;
		}
		if (of.exists() && of.lastModified() > jarFile.lastModified() && when != ExtractWhen.ALWAYS) {
			return;
		}

		if (!from.startsWith("/")) {
			from = "/" + from;
		}

		LogUtils.fine(String.format("extracting resource: %s (%s) -> %s (%s)", from, SOURCE_ENCODING.name(), to, TARGET_ENCODING.name()));

		final char[] cbuf = new char[1024];
		int read;
		try {
			final Reader in = new BufferedReader(new InputStreamReader(openResourceNoCache(from), SOURCE_ENCODING));
			final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(of), TARGET_ENCODING));
			while ((read = in.read(cbuf)) > 0) {
				out.write(cbuf, 0, read);
			}
			out.close(); in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public File getJarFile() {
		URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public InputStream openResourceNoCache(String resource) throws IOException {
		URL res = plugin.getClass().getResource(resource);
		if (res == null) {
			LogUtils.warning("can't find " + resource + " in plugin JAR file"); //$NON-NLS-1$
			return null;
		}
		URLConnection resConn = res.openConnection();
		resConn.setUseCaches(false);
		return resConn.getInputStream();
	}
}
