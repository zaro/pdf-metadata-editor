# Generate self signed certificate for code signing on Windows

Create certificate:

    $cert = New-SelfSignedCertificate -DnsName broken-by.me -Type CodeSigning -CertStoreLocation Cert:\CurrentUser\My

Set the password for it:

    $CertPassword = ConvertTo-SecureString -String "123456" -Force -AsPlainText

Export it:

    Export-PfxCertificate -Cert "cert:\CurrentUser\My\$($cert.Thumbprint)" -FilePath "win-cert.pfx" -Password $CertPassword

# Prepare windows build

```
dotnet tool install --global wix --version 6.0.2
wix extension add WiXToolset.Util.wixext

```