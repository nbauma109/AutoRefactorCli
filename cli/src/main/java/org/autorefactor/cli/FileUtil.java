package org.autorefactor.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

public class FileUtil {

	static void walkMax(final IFolder f, final int max, final IResourceVisitor visitor) throws CoreException {
	    f.accept(new IResourceVisitor() {
	        int count = max;
	
	        /** @Override */
	        public boolean visit(IResource resource) throws CoreException {
	            return count-- > 0 && visitor.visit(resource);
	        }
	    });
	}

	static String read(final IResource resource) throws CoreException {
	    return FileUtil.readFileToString(resource.getLocation().toFile().getAbsolutePath());
	}

	static void writeFile(File file, final String content) {
	    try {
	    Writer w = new FileWriter(file);
	    try {
	        w.write(content);
	        w.close();
	        w = null;
	    } finally {
	        if (w != null) {
	            try { w.close(); } catch (Exception e) {}
	        }
	    }
	    } catch (IOException e) {
	        throw new IllegalStateException(e);
	    }
	}

	/**
	 * Read file to String.
	 *
	 * TODO: use project encoding!
	 *
	 * @param filePath
	 *            file path
	 * @return String
	 * @throws CoreException
	 *             on I/O errors
	 */
	public static String readFileToString(String filePath) throws CoreException {
	    try {
	        return readFile(filePath);
	    } catch (IOException e) {
	        throw new CoreException(new Status(0, "", "", e));
	    }
	}

	/**
	 * Read file to String.
	 *
	 * TODO: use project encoding!
	 *
	 * @param filePath
	 *            file path
	 * @return String
	 * @throws CoreException
	 *             on I/O errors
	 */
	public static String readFile(String filePath) throws FileNotFoundException, IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));

		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
		    String readData = String.valueOf(buf, 0, numRead);
		    fileData.append(readData);
		    buf = new char[1024];
		}

		reader.close();

		return fileData.toString();
	}

}
