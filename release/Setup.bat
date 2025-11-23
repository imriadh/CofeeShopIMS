@echo off
echo Installing Coffee Shop App...

set "SCRIPT=%TEMP%\CreateShortcut.ps1"
echo $WshShell = New-Object -comObject WScript.Shell > "%SCRIPT%"
echo $DesktopPath = $WshShell.SpecialFolders.Item("Desktop") >> "%SCRIPT%"
echo $ShortcutPath = Join-Path $DesktopPath "Coffee Shop App.lnk" >> "%SCRIPT%"
echo $TargetDir = "%~dp0" >> "%SCRIPT%"
echo $TargetDir = $TargetDir.TrimEnd('\') >> "%SCRIPT%"
echo $TargetScript = Join-Path $TargetDir "Launch.vbs" >> "%SCRIPT%"
echo $Shortcut = $WshShell.CreateShortcut($ShortcutPath) >> "%SCRIPT%"
echo $Shortcut.TargetPath = "wscript.exe" >> "%SCRIPT%"
echo $Shortcut.Arguments = """$TargetScript""" >> "%SCRIPT%"
echo $Shortcut.WorkingDirectory = $TargetDir >> "%SCRIPT%"
echo $Shortcut.IconLocation = "javaw.exe" >> "%SCRIPT%"
echo $Shortcut.Description = "Launch Coffee Shop App" >> "%SCRIPT%"
echo $Shortcut.Save() >> "%SCRIPT%"

powershell -ExecutionPolicy Bypass -File "%SCRIPT%"
del "%SCRIPT%"

echo.
echo Success! "Coffee Shop App" shortcut has been created on your Desktop.
echo You can now close this window.
pause
