package rs.tim33.PKI.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.tim33.PKI.DTO.Organization.CreateOrgDTO;
import rs.tim33.PKI.Services.OrganizationService;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

	@Autowired
	public OrganizationService orgService;
	
	@PostMapping
	public ResponseEntity<Void> createOrganization(@RequestBody CreateOrgDTO data){
		try {
			orgService.createOrganization(data.name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
