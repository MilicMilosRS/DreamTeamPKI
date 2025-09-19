package rs.tim33.PKI.Services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rs.tim33.PKI.Models.CertificateModel;
import rs.tim33.PKI.Models.KeystoreModel;
import rs.tim33.PKI.Models.OrganizationModel;
import rs.tim33.PKI.Repositories.CertificateRepository;
import rs.tim33.PKI.Repositories.KeystoreRepository;
import rs.tim33.PKI.Repositories.OrganizationRepository;
import rs.tim33.PKI.Utils.CertificateHelper;
import rs.tim33.PKI.Utils.CertificateService;
import rs.tim33.PKI.Utils.MasterKeyProvider;

@Service
public class KeystoreService {
	@Autowired
    private KeystoreRepository keystoreRepository;
	
	@Autowired
    private CertificateRepository certRepo;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private MasterKeyProvider masterKeyProvider;

    private SecretKey decryptOrgMasterKey(byte[] encryptedKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.DECRYPT_MODE, masterKeyProvider.getMasterKey());
//        byte[] keyBytes = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(encryptedKey, "AES");
    }
    
    public KeyStore getKeystoreFromId(Long keystoreId) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    	KeystoreModel ks = keystoreRepository.findById(keystoreId).orElse(null);
    	String keystorePassword = new String(ks.getPasswordEncrypted());
        KeyStore keyStore = KeyStore.getInstance(ks.getKeystoreType());

        try (ByteArrayInputStream bais = new ByteArrayInputStream(ks.getKeystoreData())) {
            keyStore.load(bais, keystorePassword.toCharArray());
        }
        return keyStore;
    }
    
    public KeystoreModel saveKeystoreWithId(Long keystoreId, KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    	KeystoreModel ks = keystoreRepository.findById(keystoreId).orElse(null);
    	String keystorePassword = new String(ks.getPasswordEncrypted());
    	
    	try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
    		keystore.store(baos, keystorePassword.toCharArray());
            ks.setKeystoreData(baos.toByteArray());
        }
    	
    	return keystoreRepository.save(ks);
    }

    public KeystoreModel addCertificate(Long keystoreId, Long cetificateId, PrivateKey privateKey) throws Exception {
    	CertificateModel cert = certRepo.findById(cetificateId).orElse(null);
    	
//    	ByteBuffer buffer = ByteBuffer.wrap(ks.getPasswordEncrypted());
//
//        byte[] iv = new byte[12];
//        buffer.get(iv);
//        byte[] cipherText = new byte[buffer.remaining()];
//        buffer.get(cipherText);
//
//        GCMParameterSpec spec = new GCMParameterSpec(12, iv);
//    	
//    	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.DECRYPT_MODE, decryptOrgMasterKey(ks.getOrganization().getMasterKeyEncrypted()), spec);
//        byte[] passwordBytes = cipher.doFinal(cipherText);
//        String keystorePassword = new String(passwordBytes, StandardCharsets.UTF_8);
        //Get keystore
    	KeyStore keystore = getKeystoreFromId(keystoreId);
    	
    	//Add certificate
        X509Certificate ctf = CertificateHelper.getX509Certificate(cert.getCertData());
        keystore.setKeyEntry(cert.getAlias(), privateKey, "TODO PASSWORD".toCharArray(), new Certificate[] {ctf});
        
        //Persist to database
        return saveKeystoreWithId(keystoreId, keystore);
    }
    
    public KeystoreModel createKeystore(Long organizationId, String alias, String keystorePassword) throws Exception {
    	OrganizationModel org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        SecretKey orgMasterKey = decryptOrgMasterKey(org.getMasterKeyEncrypted());

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyStore.store(baos, keystorePassword.toCharArray());

//        SecureRandom random = new SecureRandom();
//        byte[] iv = new byte[12];
//        random.nextBytes(iv);
//        GCMParameterSpec spec = new GCMParameterSpec(12, iv);
//        
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.ENCRYPT_MODE, orgMasterKey, spec);
//
//        ByteBuffer buffer = ByteBuffer.allocate(iv.length + keystorePassword.getBytes(StandardCharsets.UTF_8).length);
//        buffer.put(iv);
//        buffer.put(cipher.doFinal(keystorePassword.getBytes(StandardCharsets.UTF_8)));
        
//        byte[] encryptedPassword = buffer.array();
        byte[] encryptedPassword = keystorePassword.getBytes();

        KeystoreModel ks = new KeystoreModel();
        ks.setOrganization(org);
        ks.setAlias(alias);
        ks.setKeystoreType("PKCS12");
        ks.setKeystoreData(baos.toByteArray());
        ks.setPasswordEncrypted(encryptedPassword);

        return keystoreRepository.save(ks);
    }
}
