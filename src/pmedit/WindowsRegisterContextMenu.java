package pmedit;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.W32APIFunctionMapper;

public class WindowsRegisterContextMenu {


	public static String pdfFileType(boolean create){
		String pdfFileType = null ;
		if (false){
		} else if(hasRegistryKey(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\.pdf")){
			pdfFileType = Advapi32Util.registryGetStringValue( WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\.pdf", "");
		} else if(hasRegistryKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\.pdf")){
			pdfFileType = Advapi32Util.registryGetStringValue( WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\.pdf", "");
		} else if(create){
			pdfFileType = "pdffiletype";
			Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Classes", ".pdf");
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\.pdf", "", pdfFileType);
		}
		return pdfFileType;
	}
	
	public static String shellKey(String pdfFileType){
		return "SOFTWARE\\Classes\\" + pdfFileType + "\\shell\\Pdf metadata edit";		
	}
	
	public static void createRegistryKey(String keyToCreate){
		String[] keys = keyToCreate.split("\\\\");
		String current = "";
		for(String key: keys){
			current += key;
			if(!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, current)){
				Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, current);
			}
			current += "\\";
		}
		
	}

	public static boolean hasRegistryKey(com.sun.jna.platform.win32.WinReg.HKEY root, String keyToCreate){
		String[] keys = keyToCreate.split("\\\\");
		String current = "";
		for(String key: keys){
			current += key;
			System.out.printf("\nCheck key: %s : ", current);
			if(!Advapi32Util.registryKeyExists(root, current)){
				System.out.printf("false\n");
				return false;
			}
			current += "\\";
		}
		current = current.substring(0,current.length()-1);
		System.out.printf("\nCheck key: %s\n", Advapi32Util.registryValueExists(root, current, ""));
		return Advapi32Util.registryValueExists(root, current,"");
	}


	public static void register() throws Exception{
			String pdfFileType = pdfFileType(true);
			String thisJarDir;
			try {
				thisJarDir = new File(PreferencesWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsolutePath();
				
			} catch (URISyntaxException e) {
				throw new Exception("Cannot find the path to current jar");
			}
			String shellKey = shellKey(pdfFileType);
			String shellCommandKey = shellKey +"\\command";
			createRegistryKey(shellCommandKey);
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, shellKey, "", "Pdf metadata editor");
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, shellCommandKey, "", "\"" + thisJarDir +File.separator +"PdfMetadataEditor.exe\" \"%1\"");		
	}
	
	public static void unregister() {
		String pdfFileType = pdfFileType(false);
		if(pdfFileType != null){
			String shellKey = shellKey(pdfFileType);
			String shellCommandKey = shellKey +"\\command";
			Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, shellCommandKey);
			Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, shellKey);
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0 ){
			System.out.println("Specify register or unregister as furst argument");
			return;
		}
		if(args[0].toLowerCase().equals("register")){
			try {
				register();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(args[0].toLowerCase().equals("unregister")){
			try {
				unregister();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
