/*
 * EmployerJobCreateService.java
 *
 * Copyright (C) 2012-2024 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.features.sponsor.invoice;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.data.models.Errors;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractService;
import acme.client.views.SelectChoices;
import acme.entities.sponsorships.Invoice;
import acme.entities.sponsorships.Sponsorship;
import acme.roles.Sponsor;

@Service
public class SponsorInvoiceCreateService extends AbstractService<Sponsor, Invoice> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private SponsorInvoiceRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Invoice object;
		Date moment;
		moment = MomentHelper.getCurrentMoment();
		object = new Invoice();
		object.setRegistrationTime(moment);
		super.getBuffer().addData(object);
		object.setDraftMode(true);
	}

	@Override
	public void bind(final Invoice object) {

		assert object != null;

		int sponsorshipId;
		Sponsorship sponsorship;

		sponsorshipId = super.getRequest().getData("sponsorship", int.class);
		sponsorship = this.repository.findOneSponsorshipById(sponsorshipId);
		object.setSponsorship(sponsorship);

		super.bind(object, "code", "dueDate", "invoiceQuantity", "link", "tax");

	}

	@Override
	public void validate(final Invoice object) {
		assert object != null;

		Date aboveMoment = MomentHelper.parse("2201/01/01 00:00", "yyyy/MM/dd HH:mm");

		Errors errors = super.getBuffer().getErrors();

		if (!errors.hasErrors("code")) {
			Invoice invoiceValid;
			invoiceValid = this.repository.findOneInvoiceByCode(object.getCode());
			super.state(invoiceValid == null, "code", "sponsor.sponsorship.form.error.duplicate");
		}

		if (!errors.hasErrors("dueDate")) {
			super.state(MomentHelper.isAfter(object.getDueDate(), object.getRegistrationTime()), "dueDate", "sponsor.invoice.form.error.dueDateAfterMoment");
			super.state(MomentHelper.isLongEnough(object.getRegistrationTime(), object.getDueDate(), 30, ChronoUnit.DAYS), "dueDate", "sponsor.invoice.form.error.period");
			super.state(MomentHelper.isBefore(object.getDueDate(), aboveMoment), "dueDate", "sponsor.invoide.form.error.dueDateAboveLimit");

		}

		if (!errors.hasErrors("sponsorship"))
			if (!errors.hasErrors("invoiceQuantity")) {
				super.state(object.getInvoiceQuantity().getAmount() <= object.getSponsorship().getAmount().getAmount(), "invoiceQuantity", "sponsor.invoice.form.error.outOfRange");
				super.state(object.getInvoiceQuantity().getCurrency().equals(object.getSponsorship().getAmount().getCurrency()), "invoiceQuantity", "sponsor.invoice.form.error.sponsorshipCurrency");
			}
		if (!errors.hasErrors("invoiceQuantity"))
			super.state(object.getInvoiceQuantity().getAmount() >= 0.00 && object.getInvoiceQuantity().getAmount() <= 1000000.00, "invoiceQuantity", "sponsor.invoice.form.error.outOfRange");

	}

	@Override
	public void perform(final Invoice object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Invoice object) {

		assert object != null;

		Dataset dataset;
		SelectChoices sponsorships;
		int sponsorId = super.getRequest().getPrincipal().getActiveRoleId();

		Collection<Sponsorship> unpublishedSponsorships = this.repository.findSponsorDraftModeSponsorship(sponsorId);
		sponsorships = SelectChoices.from(unpublishedSponsorships, "code", object.getSponsorship());

		dataset = super.unbind(object, "code", "registrationTime", "dueDate", "invoiceQuantity", "tax", "link", "draftMode");

		dataset.put("sponsorship", sponsorships.getSelected().getKey());
		dataset.put("sponsorships", sponsorships);

		super.getResponse().addData(dataset);
	}

}
