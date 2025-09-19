package rs.tim33.PKI.Utils;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MasterKeyProvider {

    @Value("${pki.master-key}")
    private String masterKeyBase64;

    private SecretKey masterKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
        masterKey = new SecretKeySpec(keyBytes, "AES");
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }
}