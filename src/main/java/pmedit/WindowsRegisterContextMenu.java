package pmedit;

import java.io.File;
import java.net.URISyntaxException;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

public class WindowsRegisterContextMenu {

	public static String exePath() throws Exception {
		String thisJarDir;
		try {
			thisJarDir = new File(PreferencesWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsolutePath();
			
		} catch (URISyntaxException e) {
			throw new Exception("Cannot find the path to current jar");
		}
		String appName = Version.getAppName();
		String exePath = thisJarDir + File.separator + appName + ".exe";
		if(new File(exePath).exists()){
			return exePath;
		}
		exePath = thisJarDir + File.separator + "..\\" + appName + ".exe";
		if(new File(exePath).exists()){
			return exePath;
		}
		throw new Exception("Cannot find the path to current exe");
	}
	

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
			setRegistryKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Classes\\.pdf", "", pdfFileType);
		}
		return pdfFileType;
	}
	
	public static String applicationKey(){
		return "SOFTWARE\\Classes\\" + Version.getAppName();
	}
	
	public static String editCmdShellKey(String pdfFileType){
		return "SOFTWARE\\Classes\\" + pdfFileType + "\\shell\\" + Version.getAppName() + ".Edit.File";		
	}

	public static String batchMenuShellKey(String pdfFileType){
		return "SOFTWARE\\Classes\\" + pdfFileType + "\\shell\\" + Version.getAppName() + ".Batch.Menu";		
	}

	public static String batchMenuKey(String pdfFileType){
		return "SOFTWARE\\Classes\\" + pdfFileType + "\\Batch.Menu";		
	}

	public static String batchCmdShellKey(String pdfFileType){
		return batchMenuKey(pdfFileType) + "\\shell";		
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
			//System.out.printf("\nCheck key: %s : ", current);
			if(!Advapi32Util.registryKeyExists(root, current)){
				//System.out.printf("false\n");
				return false;
			}
			current += "\\";
		}
		current = current.substring(0,current.length()-1);
		//System.out.printf("\nCheck key: %s\n", Advapi32Util.registryValueExists(root, current, ""));
		return Advapi32Util.registryValueExists(root, current,"");
	}

	public static void setRegistryKey(com.sun.jna.platform.win32.WinReg.HKEY root, String keyPath, String name, String value){
		System.out.println("Registry Create: " + keyPath + "(" + name + ")=" +value);
		try {
			Advapi32Util.registrySetStringValue(root, keyPath, name, value);			
		} catch(com.sun.jna.platform.win32.Win32Exception e){
			System.out.println(e);
		}
	}

	public static void deleteRegistryKey(com.sun.jna.platform.win32.WinReg.HKEY root, String key){
		System.out.println("Registry Delete: " + key);
		try {
			Advapi32Util.registryDeleteKey(WinReg.HKEY_CURRENT_USER, key);			
		} catch(com.sun.jna.platform.win32.Win32Exception e){
			System.out.println(e);
		}
	}
	

	public static void register() throws Exception{
			String pdfFileType = pdfFileType(true);

			
			String exePath = "\"" + exePath() + "\"";
			String shellKey = editCmdShellKey(pdfFileType);
			String shellCommandKey = shellKey +"\\command";
			String shellDdeExecKey = shellKey +"\\ddeexec";
			String shellDdeExecApplicationKey = shellDdeExecKey +"\\application";

			createRegistryKey(shellCommandKey);
			createRegistryKey(shellDdeExecKey);
			createRegistryKey(shellDdeExecApplicationKey);
			setRegistryKey(WinReg.HKEY_CURRENT_USER, shellKey, "", "Pdf metadata editor");
			setRegistryKey(WinReg.HKEY_CURRENT_USER, shellCommandKey, "", exePath );
			setRegistryKey(WinReg.HKEY_CURRENT_USER, shellDdeExecKey, "", "\"%1\"");
			setRegistryKey(WinReg.HKEY_CURRENT_USER, shellDdeExecApplicationKey, "", Version.getAppName());
			
			// Add batch commands
			String batchMenuShellKey = batchMenuShellKey(pdfFileType);
			createRegistryKey(batchMenuShellKey);
			setRegistryKey(WinReg.HKEY_CURRENT_USER, batchMenuShellKey, "MUIVerb", "Pdf metadata batch");
			setRegistryKey(WinReg.HKEY_CURRENT_USER, batchMenuShellKey, "ExtendedSubCommandsKey", pdfFileType + "\\Batch.Menu");

			for(CommandDescription desc:  CommandDescription.batchCommands){
				String batchShellKey = batchCmdShellKey(pdfFileType) + "\\" + desc.regKey;
				String batchShellCommandKey = batchShellKey + "\\command";
				String batchShellDdeExecKey = batchShellKey +"\\ddeexec";
				String batchShellDdeExecApplicationKey = batchShellKey +"\\application";
				createRegistryKey(batchShellCommandKey);
				createRegistryKey(batchShellDdeExecKey);
				createRegistryKey(batchShellDdeExecApplicationKey);
				setRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellKey, "MUIVerb", desc.description);
				setRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellCommandKey, "", exePath + " " + desc.name);
				setRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellDdeExecKey, "", desc.name +" \"%1\"");
				setRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellDdeExecApplicationKey, "", Version.getAppName());
			}
	}
	
	public static void unregister() {
		String pdfFileType = pdfFileType(false);
		if(pdfFileType != null){
			String shellKey = editCmdShellKey(pdfFileType);
			String shellCommandKey = shellKey +"\\command";
			String shellDdeExecKey = shellKey +"\\ddeexec";
			String shellDdeExecApplicationKey = shellDdeExecKey +"\\application";

			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, shellDdeExecApplicationKey);			
			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, shellDdeExecKey);
			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, shellCommandKey);
			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, shellKey);

			// Batch commands
			for(CommandDescription desc:  CommandDescription.batchCommands){
				String batchShellKey = batchCmdShellKey(pdfFileType) + "\\" + desc.regKey;
				String batchShellCommandKey = batchShellKey + "\\command";
				String batchShellDdeExecKey = batchShellKey +"\\ddeexec";
				String batchShellDdeExecApplicationKey = batchShellKey +"\\application";

				deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellDdeExecApplicationKey);
				deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellDdeExecKey);
				deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellCommandKey);
				deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchShellKey);
			}
			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchMenuShellKey(pdfFileType));
			deleteRegistryKey(WinReg.HKEY_CURRENT_USER, batchMenuKey(pdfFileType));
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0 ){
			System.out.println("Specify register or unregister as first argument");
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
