@echo off
set tfile=%TEMP%\pme-export.txt
set thisdir=%~dp0
set thisdir=%thisdir:\=\\%
reg query "HKEY_CLASSES_ROOT\.pdf" /ve | findstr Default > %tfile%

FOR /F "tokens=3" %%I IN (%tfile%) DO (
    set FTYPE=%%I
)

FOR /F "tokens=1 delims=" %%A in ('where javaw') do SET JAVAW=%%A
echo Using java at : %JAVAW%
set JAVAW=%JAVAW:\=\\%
echo %FTYPE%
echo Windows Registry Editor Version 5.00 > %tfile%
echo.  >> %tfile%
echo [HKEY_CLASSES_ROOT\%FTYPE%\shell\Pdf metadata editor] >> %tfile%
echo @="&Pdf metadata editor" >> %tfile%
echo.  >> %tfile%
echo [HKEY_CLASSES_ROOT\%FTYPE%\shell\Pdf metadata editor\command] >> %tfile%
echo @="%JAVAW% -jar \"%thisdir%pdf-metadata-edit.jar\" \"%%1\"" >> %tfile%
echo ========
type %tfile%
echo ========
regedit /s %tfile%

del %tfile%
pause