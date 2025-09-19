package rs.tim33.PKI.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rs.tim33.PKI.Models.KeystoreModel;

@Repository
public interface KeystoreRepository extends JpaRepository<KeystoreModel, Long>{

}
