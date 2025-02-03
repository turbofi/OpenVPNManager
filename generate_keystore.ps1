$programFiles = "C:\Program Files"

$serverPrivateKey = "$programFiles\openresty\ssl\localhost-server.key"
$serverPublicKey = "$programFiles\openresty\ssl\localhost-server.key.pub"
$serverPublicCert = "$programFiles\openresty\ssl\localhost-server.crt"

# Generate key store for spring java app
$keystoreDestinationFolder = "$programFiles\openvpnmanager\openvpnmanager-admin.p12"
openssl pkcs12 -export -in "$serverPublicCert" -inkey "$serverPrivateKey" -out "$keystoreDestinationFolder" -name openvpnmanager -password pass:password