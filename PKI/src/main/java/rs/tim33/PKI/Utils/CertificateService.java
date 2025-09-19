package rs.tim33.PKI.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rs.tim33.PKI.Models.CertificateModel;
import rs.tim33.PKI.Models.KeystoreModel;
import rs.tim33.PKI.Repositories.CertificateRepository;
import rs.tim33.PKI.Repositories.KeystoreRepository;
import rs.tim33.PKI.Services.KeystoreService;

@Service
public class CertificateService {
	
	public static class KeyPairAndCert {
        private final KeyPair keyPair;
        private final X509Certificate certificate;

        public KeyPairAndCert(KeyPair keyPair, X509Certificate certificate) {
            this.keyPair = keyPair;
            this.certificate = certificate;
        }
        public KeyPair getKeyPair() { return keyPair; }
        public X509Certificate getCertificate() { return certificate; }
    }

	@Autowired
	private CertificateRepository certRepo;
	@Autowired
	private KeystoreService keystoreService;
	@Autowired
	private KeystoreRepository keystoreRepo;
	
	public PrivateKey getPrivateKeyOfCert(Long certificateId) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		CertificateModel cert = certRepo.findById(certificateId).orElse(null);

		KeyStore ks = keystoreService.getKeystoreFromId(cert.getKeystore().getId());
		Key key = ks.getKey(cert.getAlias(), "TODO PASSWORD".toCharArray());
		if (key instanceof PrivateKey) {
		    return (PrivateKey) key;
		}
		
		return null;
	}
	
	public KeyPairAndCert createSelfSigned(String dn, int daysValid) throws CertificateException, CertIOException, OperatorCreationException, NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + daysValid * 24L * 60 * 60 * 1000);

        X500Name issuer = new X500Name(dn);
        BigInteger serial = BigInteger.valueOf(now);

        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        issuer, serial, startDate, endDate, issuer, keyPair.getPublic());

        //CA=true so the org can create new certificates
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

        return new KeyPairAndCert(keyPair, cert);
	}
	
	public KeyPairAndCert createIntermediate(Long parentCertId, String orgUnit, int daysValid) throws Exception {
		CertificateModel parentCert = certRepo.findById(parentCertId).orElse(null);
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
		
		long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + daysValid * 24L * 60 * 60 * 1000);
        
        X500Name subject = new X500Name("CN=Intermediate, O=" + orgUnit);
        X500Name issuer = new X500Name(parentCert.getSubjectDn());
        BigInteger serial = BigInteger.valueOf(now);
        
        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        issuer, serial, startDate, endDate, subject, keyPair.getPublic());
		
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        
        //Create certificate
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(getPrivateKeyOfCert(parentCertId));
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));
        
        //Persist it in the database
        CertificateModel newCert = new CertificateModel(cert, parentCert.getKeystore(), parentCert);
        newCert = certRepo.save(newCert);
        
        //Persist it in the keystore
        keystoreService.addCertificate(newCert.getKeystore().getId(), newCert.getId(), keyPair.getPrivate());
        System.out.println(cert.toString());
        
		return new KeyPairAndCert(keyPair, cert);
	}
}