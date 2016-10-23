/**
 * 
 */
package profile;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Read and write input profile to xml file
 * @author Yifan Ruan
 * @email  ry222ad@student.lnu.se
 */
public class ProfileExecutor {

	private static XStream xstream=new XStream(new StaxDriver());
	
	/**
	 * The constructed profile can be fetched outside
	 */
	public static InputProfile profile=new InputProfile();
	
	static{
		xstream.alias("inputProfile", InputProfile.class);
		xstream.alias("variable", InputProfileVariable.class);
		xstream.alias("value", InputProfileValue.class);
	}
	
	/**
	 * Read profile object from xml file
	 * @param xmlPath the xml file path
	 */
	public static void readFromXml(String xmlPath){
		try {
			InputStream input = new FileInputStream(xmlPath);
			ProfileExecutor.profile= (InputProfile)xstream.fromXML(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write profile object to xml file
	 * @param xmlPath the xml file path
	 */
	public static void writeToXml(String xmlPath){
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(xmlPath));	
			writer.write(xstream.toXML(ProfileExecutor.profile));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
