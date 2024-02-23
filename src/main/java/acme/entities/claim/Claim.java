
package acme.entities.claim;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import acme.client.data.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Claim extends AbstractEntity {

	// Serialisation identifier -----------------------------------------------

	private static final long	serialVersionUID	= 1L;

	// Attributes -------------------------------------------------------------

	@Pattern(regexp = "C-[0-9]{4}")
	@NotBlank
	@Column(unique = true)
	private String				code;

	@PastOrPresent
	@Temporal(TemporalType.TIMESTAMP)
	private Date				instantiationMoment;

	@NotBlank
	@Length(max = 76)
	private String				heading;

	@NotBlank
	@Length(max = 101)
	private String				description;

	@NotBlank
	@Length(max = 101)
	private String				department;

	@Column(name = "optional_email_address")
	@Email
	private String				email;

	@Column(name = "optional_link")
	@URL
	private String				link;

}
