package io.github.organizationApp.monthExpenses;

import io.github.organizationApp.categoryExpenses.CategoryType;
import io.github.organizationApp.categoryExpenses.projection.CategoryNoProcessesWriteModel;
import io.github.organizationApp.monthExpenses.projection.MonthFullReadModel;
import io.github.organizationApp.monthExpenses.projection.MonthFullWriteModel;
import io.github.organizationApp.yearExpenses.YearExpenses;
import io.github.organizationApp.yearExpenses.YearExpensesRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthExpensesServiceTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when given month parameter value doesn't represent any of english month name")
    void checkIfGivenMonthParameterValueRepresentsMonth_throwsIllegalArgumentException() {
        // given
        String testString = "TestString";
        // system under test
        var SUT = new MonthExpensesService(null, null, null, null);

        // when
        var exception = catchThrowable(() -> SUT.checkIfGivenMonthParameterValueRepresentsMonth(testString));

        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not represent the month");
    }

    @Test
    @DisplayName("Should successfully valid month name and return true")
    void checkIfGivenMonthParameterValueRepresentsMonth_returnTrue() {
        // given
        String testString1 = "january";
        String testString2 = "January";
        String testString3 = "JANUARY";
        String testString4 = "JaNUaRY";

        // system under test
        var SUT = new MonthExpensesService(null, null, null, null);

        // when
        boolean state1 = SUT.checkIfGivenMonthParameterValueRepresentsMonth(testString1);
        boolean state2 = SUT.checkIfGivenMonthParameterValueRepresentsMonth(testString2);
        boolean state3 = SUT.checkIfGivenMonthParameterValueRepresentsMonth(testString3);
        boolean state4 = SUT.checkIfGivenMonthParameterValueRepresentsMonth(testString4);

        // then
        assertThat(state1).isEqualTo(true);
        assertThat(state2).isEqualTo(true);
        assertThat(state3).isEqualTo(true);
        assertThat(state4).isEqualTo(true);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when given month value doesn't represent month")
    void save_throwsIllegalArgumentException() {
        // given
        // system under test
        var SUT = new MonthExpensesService(null, null, null, null);

        // when
        var exception = catchThrowable(() -> SUT.checkIfGivenMonthParameterValueRepresentsMonth("notMonthName"));

        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not represent the month");
    }

    @Test
    @DisplayName("Should throw NotFoundException when no year found")
    void setYearAndOwnerToNewMonth_noYear_throwsNotFoundException() {
        // given
        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findByYearAndOwnerId(anyShort(),anyString())).thenReturn(Optional.empty());
        // system under test
        var SUT = new MonthExpensesService(mockYearRepository, null, null, null);

        // when
        var exception = catchThrowable(() -> SUT.setYearAndOwnerToNewMonth((short) -1,new MonthExpenses(),"ownerId"));

        // then
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("with given id");
    }

    @Test
    @DisplayName("Should set year and owner to new month")
    void setYearAndOwnerToNewMonth_Success() throws NotFoundException {
        // given
        var mockMonth = new MonthExpenses();
        var mockYear = new YearExpenses();

        YearExpenses yearBeforeSet = mockMonth.getYear();
        String ownerBeforeSet = mockMonth.getOwnerId();

        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findByYearAndOwnerId(anyShort(),anyString())).thenReturn(Optional.of(mockYear));
        // system under test
        var SUT = new MonthExpensesService(mockYearRepository, null, null, null);

        // when
        SUT.setYearAndOwnerToNewMonth((short) -1,mockMonth,"ownerId");

        // then
        assertThat(yearBeforeSet).isNotEqualTo(mockMonth.getYear());
        assertThat(ownerBeforeSet).isNotEqualTo(mockMonth.getOwnerId());

        assertThat(mockMonth.getYear()).isEqualTo(mockYear);
        assertThat(mockMonth.getOwnerId()).isEqualTo("ownerId");
    }

    @Test
    @DisplayName("Should throw NotFoundException when no month found")
    void setMonthAndOwnerToNewCategory_noMonth_throwsNotFoundException() {
        // given
        var mockMonthRepository = mock(MonthExpensesRepository.class);
        when(mockMonthRepository.findByIdAndOwnerId(anyInt(), anyString())).thenReturn(Optional.empty());
        // system under test
        var SUT = new MonthExpensesService(null, mockMonthRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.setMonthAndOwnerToNewCategory(-1, new CategoryType(),"ownerId"));

        // then
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("no category with");
    }

    @Test
    @DisplayName("Should set month and owner to new category")
    void setMonthAndOwnerToNewCategory_Success() throws NotFoundException {
        // given
        var mockMonth = new MonthExpenses();
        var mockCategory = new CategoryType();

        MonthExpenses monthBeforeSet = mockCategory.getMonthExpenses();
        String ownerBeforeSet = mockCategory.getOwnerId();

        var mockMonthRepository = mock(MonthExpensesRepository.class);
        when(mockMonthRepository.findByIdAndOwnerId(anyInt(),anyString())).thenReturn(Optional.of(mockMonth));
        // system under test
        var SUT = new MonthExpensesService(null, mockMonthRepository, null, null);

        // when
        SUT.setMonthAndOwnerToNewCategory(-1, mockCategory, "ownerId");

        // then
        assertThat(monthBeforeSet).isNotEqualTo(mockCategory.getMonthExpenses());
        assertThat(ownerBeforeSet).isNotEqualTo(mockCategory.getOwnerId());

        assertThat(mockCategory.getMonthExpenses()).isEqualTo(mockMonth);
        assertThat(mockCategory.getOwnerId()).isEqualTo("ownerId");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when given value month doesn't represent a month")
    void createMonthWithCategories_throwsIllegalArgumentException() {
        // given
        var mockMonth = new MonthFullWriteModel();
        mockMonth.setMonth("notMonthName");
        // system under test
        var SUT = new MonthExpensesService(null, null, null, null);

        // when
        var exception = catchThrowable(() -> SUT.createMonthWithCategories(null, mockMonth, "testOwnerId"));

        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not represent the month");
    }

    @Test
    @DisplayName("Should throw a NullPointerException after trying to save in database due to the lack of real month id")
    void createMonthWithCategories_throwsNullPointerException() {
        // given
        var mockCategory1 = new CategoryNoProcessesWriteModel();
        mockCategory1.setType("TestCategory1");
        var mockCategory2 = new CategoryNoProcessesWriteModel();
        mockCategory2.setType("TestCategory2");

        MonthFullWriteModel mockMonth = mockMonthWithCategories("january", Set.of(mockCategory1, mockCategory2));
        var mockMonthRepository = inMemoryMonthRepository();
        var mockYear = new YearExpenses();

        // system under test
        var SUT = new MonthExpensesService(null, mockMonthRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.createMonthWithCategories(mockYear, mockMonth, "testOwnerId"));

        // then
        assertThat(exception)
                .isInstanceOf(NullPointerException.class);
    }

    private MonthFullWriteModel mockMonthWithCategories(String monthName, Set<CategoryNoProcessesWriteModel> mockCategories) {
        var result = mock(MonthFullWriteModel.class);
        when(result.getMonth()).thenReturn(monthName);
        List<CategoryNoProcessesWriteModel> categories = mockCategories
                .stream()
                .map(Category -> {
                    var category = mock(CategoryNoProcessesWriteModel.class);
                    when(category.getType()).thenReturn("testName");
                    when(category.getProcesses()).thenReturn(List.of());
                    return category;
                })
                .collect(Collectors.toList());
        when(result.getCategories()).thenReturn(categories);
        return result;
    }

    private InMemoryMonthRepository inMemoryMonthRepository() {
        return new InMemoryMonthRepository();
    }

    private static class InMemoryMonthRepository implements MonthExpensesRepository {
        private int index = 0;
        private Map<Integer, MonthExpenses> map = new HashMap<>();

        @Override
        public MonthExpenses save(final MonthExpenses entity) {
            if(entity.getId() == 0) {
                try {
                    var field = MonthExpenses.class.getDeclaredField("id");
                    field.setAccessible(true);
                    field.set(entity, ++index);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else map.put(index, entity);
            return entity;
        }

        @Override
        public MonthExpenses saveAndFlush(final MonthExpenses month) {
            return null;
        }

        @Override
        public List<MonthExpenses> findAllByYearIdAndOwnerId(final Integer yearId, final String ownerId) {
            return null;
        }

        @Override
        public Page<MonthExpenses> findAllByYearIdAndOwnerId(final Pageable page, final Integer yearId, final String ownerId) {
            return null;
        }

        @Override
        public Optional<MonthExpenses> findByIdAndOwnerId(final Integer id, final String ownerId) {
            return Optional.empty();
        }

        @Override
        public Optional<MonthExpenses> findByMonthAndOwnerId(final String month, final String ownerId) {
            return Optional.empty();
        }

        @Override
        public Optional<MonthExpenses> findByMonthAndYearId(final String month, final Integer yearId) {
            return Optional.empty();
        }

        @Override
        public boolean existsByMonthAndYearAndOwnerId(final String month, final YearExpenses year, final String ownerId) {
            return false;
        }

        @Override
        public void deleteByIdAndOwnerId(final Integer id, final String ownerId) {

        }
    }
}