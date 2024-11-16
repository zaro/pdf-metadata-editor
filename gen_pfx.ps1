$cert = New-SelfSignedCertificate -DnsName broken-by.me -Type CodeSigning -CertStoreLocation Cert:\CurrentUser\My

$CertPassword = ConvertTo-SecureString -String 123456 -Force -AsPlainText

Export-PfxCertificate -Cert "cert:\CurrentUser\My\$($cert.Thumbprint)" -FilePath "jpackage/cert/win-cert.pfx" -Password $CertPassword