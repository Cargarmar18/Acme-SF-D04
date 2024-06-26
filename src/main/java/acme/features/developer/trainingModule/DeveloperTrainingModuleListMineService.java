
package acme.features.developer.trainingModule;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.client.data.models.Dataset;
import acme.client.services.AbstractService;
import acme.entities.trainingModules.TrainingModule;
import acme.roles.Developer;

@Service
public class DeveloperTrainingModuleListMineService extends AbstractService<Developer, TrainingModule> {

	// Internal state ---------------------------------------------------------

	@Autowired
	private DeveloperTrainingModuleRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void authorise() {
		int developerId;
		developerId = super.getRequest().getPrincipal().getActiveRoleId();

		Developer developer;
		developer = this.repository.findDeveloperById(developerId);

		boolean status;
		status = developer != null && super.getRequest().getPrincipal().hasRole(Developer.class);
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		Collection<TrainingModule> objects;
		int developerId;

		developerId = super.getRequest().getPrincipal().getActiveRoleId();
		objects = this.repository.findManyTrainingModulesByDeveloper(developerId);

		super.getBuffer().addData(objects);
	}
	@Override
	public void unbind(final TrainingModule object) {
		assert object != null;

		Dataset dataset;

		dataset = super.unbind(object, "code", "details", "difficultyLevel", "totalTime", "draftMode");

		super.getResponse().addData(dataset);
	}
}
