package org.isf.vaccine.manager;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.TimeDuration;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.TestBuilder;
import org.isf.utils.exception.OHServiceException;
import org.isf.vaccine.TestVaccine;
import org.isf.vactype.model.VaccineType;


import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.TimeDuration;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.java.annotation.GraphWalker;
import org.graphwalker.java.test.TestBuilder;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@GraphWalker(start = "v_NoVaccines")
public class VaccineModelState2Test extends ExecutionContext implements IVaccine {

	private static TestVaccine testVaccine;
	private static List<VaccineType> testVaccineTypes;
	private static final String MAX_STRING = "x".repeat(999999999);


	private static int vaccines = 0;

	@Override
	public void v_NoVaccines() throws OHServiceException {
		System.out.println("Executing: v_NoVaccines");
	}

	@Override
	public void v_Vaccines() throws OHServiceException {
		System.out.println("Executing: v_Vaccines");

	}

	@Override
	public void e_NewVaccine() throws OHServiceException {
		System.out.println("Executing: e_NewVaccine");

	}

	@Override
	public void e_NewVaccineException() {
		System.out.println("Executing: e_NewVaccineException");
	}

	@Override
	public void e_DeleteVaccineException() throws OHServiceException {
		System.out.println("Executing: e_DeleteVaccineException");

	}


	@Override
	public void e_DeleteVaccine() throws OHServiceException {
		System.out.println("Executing: e_DeleteVaccine");

	}

	@Override
	public void e_UpdateVaccine() throws OHServiceException {
		System.out.println("Executing: e_UpdateVaccine");

	}

	@Override
	public void e_UpdateVaccineException() throws OHServiceException {
		System.out.println("Executing: e_UpdateVaccineException");


	}

	@Override
	public void e_FindVaccine() throws OHServiceException {
		System.out.println("Executing: e_FindVaccine");

	}

	@Override
	public void e_FindVaccineException() {
		System.out.println("Executing: e_FindVaccineException");

	}

	@Override
	public void e_IsCodePresent() throws OHServiceException {
		System.out.println("Executing: e_IsCodePresent");

	}

	@Override
	public void e_IsCodePresentException() {
		System.out.println("Executing: e_IsCodePresentException");

	}


	@Override
	public void v_OHException() {
		System.out.println("Executing: v_OHException");
	}

	@Override
	public void e_GetVaccine() throws OHServiceException {
		System.out.println("Executing: e_GetVaccine");

	}


	@Override
	public void e_Reset() {
		System.out.println("Executing: e_Reset");

	}


	public final static Path MODEL_PATH = Paths.get("src/main/resources/vaccine/Vaccine.json");


	@Before
	public void setUpClass() {
		vaccines = 0;
	}


	@Test
	public void runSmokeTest() {
		new TestBuilder()
			.addContext(new VaccineModelState2Test().setNextElement(new Edge().setName("e_NewVaccine").build()),
				MODEL_PATH,
				new AStarPath(new ReachedVertex("v_Vaccines")))
			.execute();
	}

	@Test
	public void runFunctionalTest() {
		new TestBuilder()
			.addContext(new VaccineModelState2Test().setNextElement(new Edge().setName("e_NewVaccine").build()),
				MODEL_PATH,
				new RandomPath(new EdgeCoverage(100)))
			.execute();
	}

	@Test
	public void runStabilityTest() {
		new TestBuilder()
			.addContext(new VaccineModelState2Test().setNextElement(new Edge().setName("e_NewVaccine").build()),
				MODEL_PATH,
				new RandomPath(new TimeDuration(30, TimeUnit.SECONDS)))
			.execute();
	}


}