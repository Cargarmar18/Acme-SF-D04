
package acme.entities.sponsorships;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import acme.client.data.AbstractEntity;
import acme.client.data.datatypes.Money;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Invoices extends AbstractEntity {

	private static final long	serialVersionUID	= 1L;

	@Column(unique = true)
	@NotBlank
	@Pattern(regexp = "^IN-[0-9]{4}-[0-9]{4}$", message = "{validation.Invoices.reference}")
	private String				code;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@PastOrPresent
	private Date				registrationTime;

	// TO DO applying constraint of 1 month
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date				dueDate;

	// 	TO DO add constraint positive or 0 on money datatype
	@NotNull
	private Money				invoiceQuantity;

	@Digits(integer = 3, fraction = 2)
	@Range(min = 0, max = 100)
	private double				tax;

	@Length(max = 255)
	@URL
	private String				link;

	// Derived attributes -----------------------------------------------------


	@Transient
	public Money totalAmount() {
		double totalAmount = this.invoiceQuantity.getAmount() + this.tax * this.invoiceQuantity.getAmount() / 100;

		Money moneyValue = new Money();

		moneyValue.setAmount(totalAmount);
		moneyValue.setCurrency(this.invoiceQuantity.getCurrency());

		return moneyValue;
	}

	// Relationships --------------------------------------------------------


	@NotNull
	@Valid
	@ManyToOne(optional = false)
	private Sponsorship sponsorship;
}
