package rs.tim33.PKI.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rs.tim33.PKI.Models.OrganizationModel;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long>{

}
