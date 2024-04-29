
package acme.features.developer.trainingSession;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.helpers.MomentHelper;
import acme.client.services.AbstractService;
import acme.client.views.SelectChoices;
import acme.entities.trainingModules.TrainingModule;
import acme.entities.trainingModules.TrainingSession;
import acme.roles.Developer;

@Service
public class DeveloperTrainingSessionCreateService extends AbstractService<Developer, TrainingSession> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private DeveloperTrainingSessionRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		TrainingSession object;

		object = new TrainingSession();
		object.setDraftMode(true);

		super.getBuffer().addData(object);
	}

	@Override
	public void bind(final TrainingSession object) {
		assert object != null;

		super.bind(object, "code", "beginningMoment", "endMoment", "location", "instructor", "contactEmail", "link", "trainingModule");
	}

	@Override
	public void validate(final TrainingSession object) {
		assert object != null;

		TrainingModule trainingModule = object.getTrainingModule();
		Date creationMoment;

		if (trainingModule != null) {
			trainingModule = object.getTrainingModule();
			creationMoment = trainingModule.getCreationMoment();

			Date lowerLimit = MomentHelper.parse("2000/01/01 00:00", "yyyy/MM/dd HH:mm");
			Date upperLimit = MomentHelper.parse("2200/12/31 23:59", "yyyy/MM/dd HH:mm");
			Date beginningUpper = MomentHelper.parse("2200/12/24 23:59", "yyyy/MM/dd HH:mm");

			if (!super.getBuffer().getErrors().hasErrors("beginningMoment")) {
				Date minimumBeginning;

				minimumBeginning = MomentHelper.deltaFromMoment(creationMoment, 7, ChronoUnit.DAYS);
				super.state(MomentHelper.isAfter(object.getBeginningMoment(), minimumBeginning), "beginningMoment", "developer.training-session.form.error.beginningMomentAfterCreationMoment");
			}

			if (!super.getBuffer().getErrors().hasErrors("beginningMoment"))
				super.state(MomentHelper.isAfter(object.getBeginningMoment(), lowerLimit), "beginningMoment", "developer.training-session.form.error.beginningMomentAfterLowerLimit");

			if (!super.getBuffer().getErrors().hasErrors("beginningMoment"))
				super.state(MomentHelper.isBefore(object.getBeginningMoment(), beginningUpper), "beginningMoment", "developer.training-session.form.error.beginningMomentBeforeLimit");

			if (!super.getBuffer().getErrors().hasErrors("endMoment")) {
				Date minimumEnd;

				minimumEnd = MomentHelper.deltaFromMoment(object.getBeginningMoment(), 7, ChronoUnit.DAYS);
				super.state(MomentHelper.isAfter(object.getEndMoment(), minimumEnd), "endMoment", "developer.training-session.form.error.endMomentAfterBeginningMoment");
			}

			if (!super.getBuffer().getErrors().hasErrors("endMoment"))
				super.state(MomentHelper.isBefore(object.getEndMoment(), upperLimit), "endMoment", "developer.training-session.form.error.endMomentBeforeUpperLimit");
		}

		if (!super.getBuffer().getErrors().hasErrors("code")) {
			TrainingSession codeValid;

			codeValid = this.repository.findOneTrainingSessionByCode(object.getCode());
			super.state(codeValid == null, "code", "developer.training-session.form.error.duplicated");
		}

		if (!super.getBuffer().getErrors().hasErrors("trainingModule"))
			super.state(trainingModule.isDraftMode() == true, "trainingModule", "developer.training-session.form.error.cannotLinkSessionsToAlreadyPublishedModules");

	}

	@Override
	public void perform(final TrainingSession object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final TrainingSession object) {
		assert object != null;
		SelectChoices choices;
		Collection<TrainingModule> trainingModules;
		int developerId;

		Dataset dataset;

		developerId = super.getRequest().getPrincipal().getActiveRoleId();
		trainingModules = this.repository.findManyTrainingModulesDraftModeByDeveloperId(developerId);
		choices = SelectChoices.from(trainingModules, "code", object.getTrainingModule());

		dataset = super.unbind(object, "code", "beginningMoment", "endMoment", "location", "instructor", "contactEmail", "link", "draftMode", "trainingModule");
		dataset.put("trainingModule", choices.getSelected().getKey());
		dataset.put("trainingModules", choices);

		super.getResponse().addData(dataset);

	}

}
