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
import rs.tim33.PKI.Repositories.OrganizationRepository;
import rs.tim33.PKI.Utils.CertificateService;
import rs.tim33.PKI.Utils.MasterKeyProvider;

@Service
public class OrganizationService {
	@Autowired
    private OrganizationRepository organizationRepository;
	@Autowired
    private CertificateRepository certificateRepo;

	@Autowired
	private KeystoreService keystoreService;
	@Autowired
	private CertificateService certService;

	@Autowired
	private MasterKeyProvider masterKeyProvider;

    public OrganizationModel createOrganization(String name) throws Exception {
        //Generate organization key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey orgMasterKey = keyGen.generateKey();

        //Encrypt the organization key with our master key
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.ENCRYPT_MODE, masterKeyProvider.getMasterKey());
//        byte[] encryptedMasterKey = cipher.doFinal(orgMasterKey.getEncoded());

        byte[] encryptedMasterKey = orgMasterKey.getEncoded();
        //Persist it
        OrganizationModel org = new OrganizationModel();
        org.setName(name);
        org.setMasterKeyEncrypted(encryptedMasterKey);
        org.setCreatedAt(LocalDateTime.now());
        org = organizationRepository.save(org);
        
        //Create a keystore for the new organization
        KeystoreModel keystore = keystoreService.createKeystore(org.getId(), org.getName(), org.getName());
        
        //Create a self-signed certificate for the new organization
        CertificateService.KeyPairAndCert certData = certService.createSelfSigned("CN=RootCA, O=" + org.getName(), 30);
        CertificateModel cert = new CertificateModel(certData.getCertificate(), keystore, null);
        certificateRepo.save(cert);

        keystoreService.addCertificate(keystore.getId(), cert.getId(), certData.getKeyPair().getPrivate());
        
        return org;
    }
}
