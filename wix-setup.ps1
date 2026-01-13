# Run with:
#   powershell.exe -ExecutionPolicy Bypass -File wix-setup.ps1

# Install WiX tool
dotnet tool install --global wix --version 6.0.2

# Set WIX_EXTENSIONS to current directory + modules\editor-gui\
$env:WIX_EXTENSIONS = Join-Path $PWD.Path "modules\editor-gui"

# Add extensions
wix extension add --global WixToolset.Util.wixext
wix extension add --global WixToolset.UI.wixext

# List installed extensions
wix extension list

# Verify the environment variable is set
Write-Host "WIX_EXTENSIONS is set to: $env:WIX_EXTENSIONS"