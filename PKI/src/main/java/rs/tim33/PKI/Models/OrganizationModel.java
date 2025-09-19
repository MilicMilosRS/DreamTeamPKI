package rs.tim33.PKI.Models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrganizationModel {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    private byte[] masterKeyEncrypted; // master key organizacije šifrovan sistemskim master key-om

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<KeystoreModel> keystores;
}
