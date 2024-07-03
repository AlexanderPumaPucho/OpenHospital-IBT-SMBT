package org.isf.vaccine.manager;

import org.isf.OHCoreTestCase;
import org.isf.utils.exception.OHDataIntegrityViolationException;
import org.isf.utils.exception.OHDataValidationException;
import org.isf.utils.exception.OHException;
import org.isf.utils.exception.OHServiceException;
import org.isf.vaccine.TestVaccine;
import org.isf.vaccine.model.Vaccine;
import org.isf.vaccine.service.VaccineIoOperationRepository;
import org.isf.vaccine.service.VaccineIoOperations;
import org.isf.vactype.model.VaccineType;
import org.isf.vactype.service.VaccineTypeIoOperationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaccineInterfaceTest extends OHCoreTestCase {

	private static TestVaccine testVaccine;
	private static List<VaccineType> testVaccineTypes;
	private static final String MAX_STRING = "x".repeat(999999999);

	@Autowired
	VaccineIoOperations vaccineIoOperation;
	@Autowired
	VaccineIoOperationRepository vaccineIoOperationRepository;
	@Autowired
	VaccineTypeIoOperationRepository vaccineTypeIoOperationRepository;
	@Autowired
	VaccineBrowserManager vaccineBrowserManager;

	@BeforeAll
	static void setUpClass() {
		testVaccineTypes = new ArrayList<VaccineType>();
	}


	@BeforeEach
	void setUp() {
		cleanH2InMemoryDb();
		testVaccineTypes = setupTestVaccineTypes();
	}

	//***************** New Vaccine Test Cases *****************
	static Collection<Arguments> dataTableNewVaccine() {
		return Arrays.asList(
			// Vaccine -- IsVaccinePresentInTheDB -- ExpectedCodeValue -- expectedException
			Arguments.of(new Vaccine("a", "2", getVaccineType("C")), false, "a", null),
			Arguments.of(new Vaccine("#TestCode1", MAX_STRING, getVaccineType("C")), false, "#TestCode1", null),
			Arguments.of(new Vaccine(null, "2", getVaccineType("C")), false, null, NullPointerException.class),
			Arguments.of(new Vaccine("", MAX_STRING, getVaccineType("C")), false, null, OHDataValidationException.class),
			Arguments.of(new Vaccine("#TestCode11", "2", getVaccineType("C")), false, null, OHDataValidationException.class),
			Arguments.of(new Vaccine(MAX_STRING, MAX_STRING, getVaccineType("C")), false, null, OHDataValidationException.class),
			Arguments.of(new Vaccine("a", "2", getVaccineType("C")), true, null, OHDataIntegrityViolationException.class),
			Arguments.of(new Vaccine("#TestCode1", MAX_STRING, getVaccineType("C")), true, null, OHDataIntegrityViolationException.class),
			Arguments.of(new Vaccine("a", null, getVaccineType("C")), false, null, NullPointerException.class),
			Arguments.of(new Vaccine("#TestCode1", "", getVaccineType("C")), false, null, OHDataValidationException.class),
			Arguments.of(new Vaccine("a", "2", getVaccineType("U")), false, null, OHServiceException.class),
			Arguments.of(new Vaccine("#TestCode1", MAX_STRING, getVaccineType("U")), false, null, OHServiceException.class)
		);
	}

	@ParameterizedTest
	@MethodSource(value = "dataTableNewVaccine")
	void newVaccineTest(Vaccine vaccine, boolean isVaccinePresent, String expectedCodeValue, Class<? extends Throwable> expectedException) throws OHServiceException {
		if (isVaccinePresent)
			setupTestNewVaccine(vaccine.getCode());

		if (expectedException == null) {
			String newVaccineCode = vaccineBrowserManager.newVaccine(vaccine).getCode();
			assertThat(newVaccineCode).isEqualTo(expectedCodeValue);
		} else {
			assertThrows(expectedException, () -> vaccineBrowserManager.newVaccine(vaccine));
		}
	}

	//***************** Update Vaccine Test Cases *****************
	static Collection<Arguments> dataTableUpdateVaccine() {
		return Arrays.asList(
			// Vaccine -- IsPresent -- Description -- Vaccine-Type -- ExpectedCode -- ExpectedException
			Arguments.of(new Vaccine("b", "c", getVaccineType("P")), true, "b", null),
			Arguments.of(new Vaccine("#TestCode2", MAX_STRING, getVaccineType("P")), true, "#TestCode2", null),
			Arguments.of(new Vaccine(null, "c", getVaccineType("P")), true, null, NullPointerException.class),
			Arguments.of(new Vaccine(null, MAX_STRING, getVaccineType("P")), true, null, NullPointerException.class),
			Arguments.of(new Vaccine("#TestCode12", "c", getVaccineType("P")), true, null, OHDataValidationException.class),
			Arguments.of(new Vaccine(MAX_STRING, MAX_STRING, getVaccineType("P")), true, null, OHDataValidationException.class),
			Arguments.of(new Vaccine("b", "c", getVaccineType("P")), false, null, OHDataIntegrityViolationException.class),
			Arguments.of(new Vaccine("#TestCode2", MAX_STRING, getVaccineType("P")), false, null, OHDataIntegrityViolationException.class),
			Arguments.of(new Vaccine("b", null, getVaccineType("P")), true, null, NullPointerException.class),
			Arguments.of(new Vaccine("#TestCode2", null, getVaccineType("P")), true, null, NullPointerException.class),
			Arguments.of(new Vaccine("b", "c", getVaccineType("W")), true, null, OHServiceException.class),
			Arguments.of(new Vaccine("#TestCode2", MAX_STRING, getVaccineType("W")), true, null, OHServiceException.class)
		);
	}

	@ParameterizedTest
	@MethodSource(value = "dataTableUpdateVaccine")
	void updateVaccineTest(Vaccine vaccine, boolean isVaccinePresent, String expectedCodeValue, Class<? extends Throwable> expectedException) throws OHServiceException {
		if (expectedException == null) {
			if (isVaccinePresent)
				setupTestNewVaccine(vaccine.getCode());
			Vaccine foundVaccine = vaccineBrowserManager.findVaccine(vaccine.getCode());
			foundVaccine.setCode(vaccine.getCode());
			foundVaccine.setDescription(vaccine.getDescription());
			foundVaccine.setVaccineType(vaccine.getVaccineType());
			String updateVaccineCode = vaccineBrowserManager.updateVaccine(foundVaccine).getCode();
			assertThat(updateVaccineCode).isEqualTo(expectedCodeValue);
		} else {
			assertThrows(expectedException, () -> vaccineBrowserManager.updateVaccine(vaccine));
		}
	}

	//***************** Delete Vaccine Test Cases *****************
	static Collection<Arguments> dataTableDeleteVaccine() {
		return Arrays.asList(
			// VaccineCode -- IsPresent -- ExpectedCode -- ExpectedException
			Arguments.of("d", true, null, null),
			Arguments.of("#TestCode3", true, null, null),
			Arguments.of(null, true, null, NullPointerException.class),
			Arguments.of("#TestCode13", true, null, OHDataValidationException.class),
			Arguments.of(MAX_STRING, true, null, OHDataValidationException.class),
			Arguments.of("d", false, null, OHServiceException.class),
			Arguments.of("#TestCode3", false, null, OHServiceException.class)
		);
	}

	@ParameterizedTest
	@MethodSource(value = "dataTableDeleteVaccine")
	void deleteVaccineTest(String vaccineCode, boolean isVaccinePresent, String expectedCodeValue, Class<? extends Throwable> expectedException) throws OHException, OHServiceException {
		if (expectedException == null) {
			if (isVaccinePresent)
				setupTestNewVaccine(vaccineCode);
			Vaccine foundVaccine = vaccineBrowserManager.findVaccine(vaccineCode);
			vaccineBrowserManager.deleteVaccine(foundVaccine);
			assertThat(vaccineIoOperation.isCodePresent(vaccineCode)).isFalse();
		} else {
			assertThrows(expectedException, () -> vaccineBrowserManager.deleteVaccine(setupTestVaccine(vaccineCode)));
		}
	}

	//***************** Get Vaccine Test Cases *****************
	static Collection<Arguments> dataTableGetVaccine() {

		return Arrays.asList(
			// VaccineTypeCode -- ExpectedSizeList 
			Arguments.of("N", 1),
			Arguments.of("O", 0));
	}

	@ParameterizedTest
	@MethodSource(value = "dataTableGetVaccine")
	void getVaccineTest(String vaccineTypeCode, int expectedSizeList) throws OHException, OHServiceException {
		setupTestNewVaccine("vaccineCodeTest");
		assertThat(vaccineBrowserManager.getVaccine(vaccineTypeCode).size()).isEqualTo(expectedSizeList);
	}


	//***************** Is Code Present Test Cases *****************
	static Collection<Arguments> dataTableIsCodePresent() {

		return Arrays.asList(
			// VaccineTypeCode -- ExpectedValue -- ExpectedException
			Arguments.of("f", true, null),
			Arguments.of("#TestCode4", false, null),
			Arguments.of(null, false, OHServiceException.class),
			Arguments.of("#TestCode14", false, OHDataValidationException.class),
			Arguments.of(MAX_STRING, false, OHDataValidationException.class)
		);

	}

	@ParameterizedTest
	@MethodSource(value = "dataTableIsCodePresent")
	void isCodePresentTest(String vaccineCode, boolean expectedValue, Class<? extends Throwable> expectedException) throws OHException, OHServiceException {
		if (expectedException == null) {
			setupTestNewVaccine("f");
			assertThat(vaccineBrowserManager.isCodePresent(vaccineCode)).isEqualTo(expectedValue);
		} else {
			assertThrows(expectedException, () -> vaccineBrowserManager.isCodePresent(vaccineCode));
		}
	}


	//***************** Find Vaccine Test Cases *****************
	static Collection<Arguments> dataTableFindVaccine() {

		return Arrays.asList(
			// VaccineTypeCode -- IsCodePresent --ExpectedCodeValue -- ExpectedException
			Arguments.of("g", true, "g", null),
			Arguments.of("#TestCode5", true, "#TestCode5", null),
			Arguments.of(null, true, null, OHServiceException.class),
			Arguments.of("#TestCode15", true, null, OHDataValidationException.class),
			Arguments.of(MAX_STRING, true, null, OHDataValidationException.class),
			Arguments.of("g", false, null, null),
			Arguments.of("#TestCode5", false, null, null));

	}


	@ParameterizedTest
	@MethodSource(value = "dataTableFindVaccine")
	void findVaccineTest(String vaccineCode, boolean isVaccinePresent, String expectedValue, Class<? extends Throwable> expectedException) throws OHException, OHServiceException {
		if (expectedException == null) {
			if (isVaccinePresent)
				setupTestNewVaccine(vaccineCode);
			Vaccine foundVaccine = vaccineBrowserManager.findVaccine(vaccineCode);
			if (foundVaccine == null)
				assertThat(foundVaccine).isEqualTo(expectedValue);
			else
				assertThat(foundVaccine.getCode()).isEqualTo(expectedValue);

		} else {
			assertThrows(expectedException, () -> vaccineBrowserManager.findVaccine(vaccineCode));
		}
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

	private void setupTestNewVaccine(String vaccineCode) {

		vaccineIoOperationRepository.saveAndFlush(setupTestVaccine(vaccineCode));
	}

	private Vaccine setupTestVaccine(String vaccineCode) {
		return new Vaccine(vaccineCode, "Default description", getVaccineType("N"));
	}


}