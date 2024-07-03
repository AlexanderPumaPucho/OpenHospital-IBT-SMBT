package org.isf.vaccine.manager;

import org.graphwalker.java.annotation.GraphWalker;
import org.isf.OHCoreTestCase;
import org.isf.utils.exception.OHServiceException;
import org.isf.vaccine.TestVaccine;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccine.service.VaccineIoOperationRepository;
import org.isf.vaccine.service.VaccineIoOperations;
import org.isf.vactype.model.VaccineType;
import org.isf.vactype.service.VaccineTypeIoOperationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@GraphWalker(start = "v_NoVaccines")
public class VaccineModelStateTest extends OHCoreTestCase implements IVaccine {


	private static List<VaccineType> testVaccineTypes;
	private static final String MAX_STRING = "x".repeat(999999999);
	private static int vaccines = 0;

	@Autowired
	VaccineIoOperations vaccineIoOperation;
	@Autowired
	VaccineIoOperationRepository vaccineIoOperationRepository;
	@Autowired
	VaccineTypeIoOperationRepository vaccineTypeIoOperationRepository;
	@Autowired
	VaccineBrowserManager vaccineBrowserManager;

	@Override
	public void v_NoVaccines() throws OHServiceException {
		System.out.println("Executing: v_NoVaccines");
		assertThat(vaccines).isEqualTo(0);
		try {
			assertThat(vaccineBrowserManager.getVaccine().size()).isEqualTo(0);
		} catch (Exception e) {
			assertThrows(OHServiceException.class, () -> vaccineBrowserManager.getVaccine());
		}
		vaccines = 0;
	}

	@Override
	public void v_Vaccines() throws OHServiceException {
		System.out.println("Executing: v_Vaccines");
		assertThat(vaccines).isGreaterThan(0);
		assertThat(vaccineBrowserManager.getVaccine().size()).isGreaterThan(0);
	}

	@Override
	public void e_NewVaccine() throws OHServiceException {
		System.out.println("Executing: e_NewVaccine");
		Vaccine newVaccine = setupTestVaccine(Integer.toString(vaccines));
		String newVaccineCode = vaccineBrowserManager.newVaccine(newVaccine).getCode();
		assertThat(newVaccineCode).isEqualTo(newVaccine.getCode());
		vaccines++;
	}

	@Override
	public void e_NewVaccineException() {
		System.out.println("Executing: e_NewVaccineException");
		Vaccine newVaccine = setupTestVaccine(Integer.toString(vaccines));
		newVaccine.setVaccineType(getVaccineType("U"));
		assertThrows(OHServiceException.class, () -> vaccineBrowserManager.newVaccine(newVaccine));
	}

	@Override
	public void e_DeleteVaccineException() throws OHServiceException {
		System.out.println("Executing: e_DeleteVaccineException");
		Vaccine deletedVaccine = setupTestVaccine("Default");
		assertThat(vaccineIoOperation.isCodePresent(deletedVaccine.getCode())).isFalse();
		assertThrows(OHServiceException.class, () -> vaccineBrowserManager.deleteVaccine(deletedVaccine));
	}


	@Override
	public void e_DeleteVaccine() throws OHServiceException {
		System.out.println("Executing: e_DeleteVaccine");
		Vaccine foundVaccine = vaccineBrowserManager.getVaccine().stream().findFirst().get();
		//.findVaccine(vaccineCode);
		vaccineBrowserManager.deleteVaccine(foundVaccine);
		assertThat(vaccineIoOperation.isCodePresent(foundVaccine.getCode())).isFalse();
		vaccines--;

	}

	@Override
	public void e_UpdateVaccine() throws OHServiceException {
		System.out.println("Executing: e_UpdateVaccine");
		Vaccine foundVaccine = vaccineBrowserManager.getVaccine().stream().findFirst().get();
		String expectedDescriptionValue = foundVaccine.getDescription() + "Updated";
		String expectedCodeTypeValue = testVaccineTypes.stream().filter(e -> !e.equals(foundVaccine.getVaccineType())).findAny().get().getCode();
		foundVaccine.setDescription(expectedDescriptionValue);
		foundVaccine.setVaccineType(getVaccineType(expectedCodeTypeValue));
		String updateVaccineDescription = vaccineBrowserManager.updateVaccine(foundVaccine).getDescription();
		String updateVaccineTypeCode = vaccineBrowserManager.updateVaccine(foundVaccine).getVaccineType().getCode();
		assertThat(updateVaccineDescription).isEqualTo(expectedDescriptionValue);
		assertThat(updateVaccineTypeCode).isEqualTo(expectedCodeTypeValue);
	}

	@Override
	public void e_UpdateVaccineException() throws OHServiceException {
		System.out.println("Executing: e_UpdateVaccineException");

		var vaccines = vaccineBrowserManager.getVaccine();
		Vaccine vaccine;
		if (!vaccines.isEmpty()) {
			vaccine = vaccines.stream().findAny().get();
			vaccine.setCode("");
			vaccine.setDescription("");

		} else {
			vaccine = setupTestVaccine(MAX_STRING);

		}
		assertThrows(OHServiceException.class, () -> vaccineBrowserManager.updateVaccine(vaccine));


	}

	@Override
	public void e_FindVaccine() throws OHServiceException {
		System.out.println("Executing: e_FindVaccine");
		var vaccines = vaccineBrowserManager.getVaccine();
		if (!vaccines.isEmpty()) {
			Vaccine vaccine = vaccines.stream().findAny().get();
			String expectedCodeValue = vaccine.getCode();
			String updateVaccineCode = vaccineBrowserManager.findVaccine(vaccine.getCode()).getCode();
			assertThat(updateVaccineCode).isEqualTo(expectedCodeValue);
		} else {
			assertThat(vaccineBrowserManager.findVaccine("Default")).isEqualTo(null);
		}
	}

	@Override
	public void e_FindVaccineException() {
		System.out.println("Executing: e_FindVaccineException");
		assertThrows(OHServiceException.class, () -> vaccineBrowserManager.findVaccine(null));
	}

	@Override
	public void e_IsCodePresent() throws OHServiceException {
		System.out.println("Executing: e_IsCodePresent");
		var vaccines = vaccineBrowserManager.getVaccine();
		if (!vaccines.isEmpty()) {
			Vaccine foundVaccine = vaccineBrowserManager.getVaccine().stream().findFirst().get();
			assertTrue(vaccineBrowserManager.isCodePresent(foundVaccine.getCode()));
		} else {
			assertFalse(vaccineBrowserManager.isCodePresent("Default"));
		}

	}

	@Override
	public void e_IsCodePresentException() {
		System.out.println("Executing: e_IsCodePresentException");
		assertThrows(OHServiceException.class, () -> vaccineBrowserManager.isCodePresent(null));
	}


	@Override
	public void v_OHException() {
		System.out.println("Executing: v_OHException");
	}

	@Override
	public void e_GetVaccine() throws OHServiceException {
		System.out.println("Executing: e_GetVaccine");
		assertThat(vaccineBrowserManager.getVaccine().size()).isEqualTo(vaccines);
	}


	@Override
	public void e_Reset() {
		System.out.println("Executing: e_Reset");
		vaccines = 0;
		try {
			cleanH2InMemoryDb();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public final static Path MODEL_PATH = Paths.get("src/main/resources/vaccine/Vaccine.json");


	@BeforeAll
	static void setUpClass() {

		vaccines = 0;
		testVaccineTypes = new ArrayList<VaccineType>();
	}

	@BeforeEach
	void setUp() {
		cleanH2InMemoryDb();
		vaccines = 0;
		testVaccineTypes = setupTestVaccineTypes();
	}

//	@Test
//	public void runSmokeTest() {
//		new TestBuilder()
//			.addContext(new VaccineModelStateTest().setNextElement(new Edge().setName("e_NewVaccine").build()),
//				MODEL_PATH,
//				new AStarPath(new ReachedVertex("v_Vaccines")))
//			.execute();
//	}
//
//	@Test
//	public void runFunctionalTest() {
//		new TestBuilder()
//			.addContext(new VaccineModelStateTest().setNextElement(new Edge().setName("e_NewVaccine").build()),
//				MODEL_PATH,
//				new RandomPath(new EdgeCoverage(100)))
//			.execute();
//	}
//
//	@Test
//	public void runStabilityTest() {
//		new TestBuilder()
//			.addContext(new VaccineModelStateTest().setNextElement(new Edge().setName("e_NewVaccine").build()),
//				MODEL_PATH,
//				new RandomPath(new TimeDuration(30, TimeUnit.SECONDS)))
//			.execute();
//	}

	@Test
	public void testCase1() throws OHServiceException {
		v_NoVaccines();
		e_IsCodePresentException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase2() throws OHServiceException {
		v_NoVaccines();
		e_DeleteVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase3() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_DeleteVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase4() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase5() throws OHServiceException {
		v_NoVaccines();
		e_IsCodePresent();
		v_NoVaccines();
	}

	@Test
	public void testCase6() throws OHServiceException {
		v_NoVaccines();
		e_UpdateVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase7() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase8() throws OHServiceException {
		v_NoVaccines();
		e_FindVaccine();
		v_NoVaccines();
	}

	@Test
	public void testCase9() throws OHServiceException {
		v_NoVaccines();
		e_GetVaccine();
		v_NoVaccines();
	}

	@Test
	public void testCase10() throws OHServiceException {
		v_NoVaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase11() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_DeleteVaccine();
		v_NoVaccines();
	}

	@Test
	public void testCase12() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase13() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_IsCodePresentException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase14() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase15() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_UpdateVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_DeleteVaccine();
		v_NoVaccines();
	}


	@Test
	public void testCase16() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase17() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_UpdateVaccine();
		v_Vaccines();
		e_DeleteVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase18() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase19() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_IsCodePresent();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_UpdateVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_DeleteVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase20() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_UpdateVaccine();
		v_Vaccines();
		e_UpdateVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}


	@Test
	public void testCase21() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_NewVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase22() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_UpdateVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase23() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_DeleteVaccine();
		v_NoVaccines();
	}

	@Test
	public void testCase24() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_FindVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_UpdateVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}


	@Test
	public void testCase25() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}


	@Test
	public void testCase26() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}


	@Test
	public void testCase27() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_IsCodePresent();
		v_Vaccines();
		e_NewVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase28() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_GetVaccine();
		v_Vaccines();
		e_FindVaccineException();
		v_OHException();
		e_Reset();
		v_NoVaccines();
	}

	@Test
	public void testCase29() throws OHServiceException {
		v_NoVaccines();
		e_NewVaccine();
		v_Vaccines();
		e_NewVaccine();
		v_Vaccines();
		e_DeleteVaccine();
		v_Vaccines();
		e_DeleteVaccine();
		v_NoVaccines();
	}

	private List<VaccineType> setupTestVaccineTypes() {
		List<VaccineType> vaccineTypes = List.of(
			new VaccineType("C", "Child"),
			new VaccineType("N", "No pregnant"),
			new VaccineType("P", "Pregnant"));
		vaccineTypes.forEach(e -> vaccineTypeIoOperationRepository.saveAndFlush(e));
		return vaccineTypes;
	}

	private static VaccineType getVaccineType(String vaccineTypeCode) {
		return testVaccineTypes.stream()
			.filter(o -> o.getCode().equals(vaccineTypeCode))
			.findFirst()
			.orElse(new VaccineType(vaccineTypeCode, "Default Description"));
	}

	private Vaccine setupTestVaccine(String vaccineCode) {
		return new Vaccine(vaccineCode, "Default description", getVaccineType("N"));
	}

}