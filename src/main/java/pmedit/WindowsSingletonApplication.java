package pmedit;

import java.io.File;
import java.net.URISyntaxException;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import static com.sun.jna.platform.win32.Tlhelp32.TH32CS_SNAPALL;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Psapi;



public class WindowsSingletonApplication {

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

	public static void killProcessByName(String filename) {
			int thisProcessId = Kernel32.INSTANCE.GetCurrentProcessId();

			final byte[] filePathUnicode = new byte[1025];
			int length = Psapi.INSTANCE.GetModuleFileNameExA(null, null, filePathUnicode, filePathUnicode.length - 1);
			if (length == 0) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}

			String thisProcessImageName = Native.toString(filePathUnicode).substring(0, length);

			HANDLE hSnapShot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(TH32CS_SNAPALL, null);
			Tlhelp32.PROCESSENTRY32 process = new Tlhelp32.PROCESSENTRY32();
			boolean hRes = Kernel32.INSTANCE.Process32First(hSnapShot, process);
			while (hRes) {
					String imageName = Native.toString(process.szExeFile);
					if (thisProcessId != process.th32ProcessID.intValue() && imageName.equalsIgnoreCase(thisProcessImageName)) {
					}
					hRes = Kernel32.INSTANCE.Process32Next(hSnapShot, process);
			}
			Kernel32.INSTANCE.CloseHandle(hSnapShot);
	}


}
