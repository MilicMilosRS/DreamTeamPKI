package rs.tim33.PKI.Services;

import java.time.LocalDateTime;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rs.tim33.PKI.Controllers.CertificateController;
import rs.tim33.PKI.Models.CertificateModel;
import rs.tim33.PKI.Models.KeystoreModel;
import rs.tim33.PKI.Models.OrganizationModel;
import rs.tim33.PKI.Repositories.CertificateRepository;
import rs.tim33.PKI.Repositories.KeystoreRepository;
import rs.tim33.PKI.Repositories.OrganizationRepository;
import rs.tim33.PKI.Utils.CertificateService;
import rs.tim33.PKI.Utils.KeyHelper;

@Service
public class OrganizationService {
	@Autowired
    private OrganizationRepository organizationRepository;
	@Autowired
    private CertificateRepository certificateRepo;
	@Autowired
	private KeystoreRepository keystoreRepo;
	
	@Autowired
	private KeystoreService keystoreService;
	@Autowired
	private CertificateService certService;

	@Autowired
	private KeyHelper keyHelper;

    public OrganizationModel createOrganization(String name) throws Exception {
    	//Generate key for organisation keystore
        byte[] encryptedMasterKey = keyHelper.generateEncryptedKeystoreKey();
        
        //Persist it
        OrganizationModel org = new OrganizationModel();
        org.setName(name);
        org.setMasterKeyEncrypted(encryptedMasterKey);
        org.setCreatedAt(LocalDateTime.now());
        org = organizationRepository.save(org);
        
        //Create a keystore for the new organization
        KeystoreModel keystore = keystoreService.createKeystore(org.getName(), keyHelper.decryptKeystoreKey(encryptedMasterKey).getEncoded());
        keystore.setOrganization(org);
        keystoreRepo.save(keystore);
        
        //Create a self-signed certificate for the new organization
        CertificateService.KeyPairAndCert certData = certService.createSelfSigned("CN=RootCA, O=" + org.getName(), 30);
        CertificateModel cert = new CertificateModel(certData.getCertificate(), keystore, null);
        certificateRepo.save(cert);

        //Add it to the keystore
        keystoreService.addCertificate(keystore.getId(), cert.getId(), certData.getKeyPair().getPrivate());
        
        return org;
    }
}
