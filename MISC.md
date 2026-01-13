# Generate self signed certificate for code signing on Windows

Create certificate:

    $cert = New-SelfSignedCertificate -DnsName pdf.metadata.care -Type CodeSigning -CertStoreLocation Cert:\CurrentUser\My

Set the password for it:

    $CertPassword = ConvertTo-SecureString -String "123456" -Force -AsPlainText

Export it:

    Export-PfxCertificate -Cert "cert:\CurrentUser\My\$($cert.Thumbprint)" -FilePath "win-cert.pfx" -Password $CertPassword

