Set oShell = CreateObject ("Wscript.Shell") 

strPath = Wscript.ScriptFullName
Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objFile = objFSO.GetFile(strPath)
strFolder = objFSO.GetParentFolderName(objFile) 

strArgs = "javaw -jar " & strFolder & "\pdf-metadata-edit.jar"

if WScript.Arguments.Count > 0 then
	strArgs = strArgs & " """& WScript.Arguments(0) & """"
end if
oShell.Run strArgs, 0, false