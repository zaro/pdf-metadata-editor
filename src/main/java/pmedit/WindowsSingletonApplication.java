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

	public static boolean isAlreadyRunning() {
			int thisProcessId = Kernel32.INSTANCE.GetCurrentProcessId();

			final byte[] filePathUnicode = new byte[1025];
			int length = Psapi.INSTANCE.GetModuleFileNameExA(Kernel32.INSTANCE.GetCurrentProcess(), null, filePathUnicode, filePathUnicode.length - 1);
			if (length == 0) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
			}

			String thisProcessImageName = Native.toString(filePathUnicode).substring(0, length);

			HANDLE hSnapShot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(TH32CS_SNAPALL, null);
			Tlhelp32.PROCESSENTRY32 process = new Tlhelp32.PROCESSENTRY32();
			boolean hRes = Kernel32.INSTANCE.Process32First(hSnapShot, process);
			boolean result = false;
			while (hRes) {
				HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,	false, process.th32ProcessID.intValue());
				if (hProcess != null) {
					length = Psapi.INSTANCE.GetModuleFileNameExA(hProcess, null, filePathUnicode, filePathUnicode.length - 1);
					Kernel32.INSTANCE.CloseHandle(hProcess);
					if (length == 0) {
							throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
					}
					String imageName = Native.toString(filePathUnicode).substring(0, length);
					if (thisProcessId != process.th32ProcessID.intValue() && imageName.equalsIgnoreCase(thisProcessImageName)) {
						result = true;
						break;
					}
				}
				hRes = Kernel32.INSTANCE.Process32Next(hSnapShot, process);
			}
			Kernel32.INSTANCE.CloseHandle(hSnapShot);
			return result;
	}


}
