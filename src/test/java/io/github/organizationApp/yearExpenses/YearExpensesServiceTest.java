package io.github.organizationApp.yearExpenses;

import io.github.organizationApp.monthExpenses.MonthExpenses;
import io.github.organizationApp.monthExpenses.MonthExpensesRepository;
import io.github.organizationApp.monthExpenses.MonthExpensesService;
import javassist.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.when;

class YearExpensesServiceTest {

    @Test
    @DisplayName("Should throw NotFoundException when no year found")
    void setYearAndOwnerToNewMonth_throwsNotFoundException() {
        //given
        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findByYearAndOwnerId(anyShort(),anyString())).thenReturn(null);
        // system under test
        var SUT = new YearExpensesService(mockYearRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.setYearAndOwnerToNewMonth(0,null,"123gge"));

        // then
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("with given id");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when given month name value doesn't represent english month names")
    void addMonth_wrongMonthName_throwsIllegalArgumentException() {
        // given
        var mockMonth = new MonthExpenses("notMonthName",null,null,"userUniqueId1245");
        var mockMonthService = mock(MonthExpensesService.class);
        // system under test
        var SUT = new YearExpensesService(null, null, mockMonthService);

        // when
        var exception = catchThrowable(() -> SUT.addMonth(mockMonth));

        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not represent the month");
    }

    @Test
    @DisplayName("Should return empty list when no years found and monthsFlagChosen is set true")
    void findAll_NoYears_monthsFlagChosen_returnsEmptyList() {
        // given
        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findAllByOwnerId(anyString())).thenReturn(List.of());
        // system under test
        var SUT = new YearExpensesService(mockYearRepository, null, null);

        // when
        var result = SUT.findAll(true,anyString());

        //then
        assertThat(result).isEqualTo(List.of());
    }

    @Test
    @DisplayName("Should return empty list when no years found and monthsFlagChosen is set false")
    void findAll_NoYears_NoMonthsFlagChosen_returnsEmptyList() {
        // given
        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findAllByOwnerId(anyString())).thenReturn(List.of());
        // system under test
        var SUT = new YearExpensesService(mockYearRepository, null, null);

        // when
        var result = SUT.findAll(false, anyString());

        //then
        assertThat(result).isEqualTo(List.of());
    }

    @Test
    @DisplayName("Should throw NullPointerException when YearRepository returns null instead of List")
    void findAll_nullInsteadOfYearsList_throwsNullPointerException() {
        // given
        var mockYearRepository = mock(YearExpensesRepository.class);
        when(mockYearRepository.findAllByOwnerId(anyString())).thenReturn(null);
        // system under test
        var SUT = new YearExpensesService(mockYearRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.findAll(anyBoolean(),"ownerId1234"));

        //then
        assertThat(exception)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("no years");
    }

    @Test
    @DisplayName("Should return an empty list of months")
    void findAllMonthsBelongToYear_noMonths_returnsEmptyList() {
        // given
        var mockMonthRepository = mock(MonthExpensesRepository.class);
        when(mockMonthRepository.findAllByYearIdAndOwnerId(anyInt(), anyString())).thenReturn(List.of());
        // system under test
        var SUT = new YearExpensesService(null, mockMonthRepository, null);

        // when
        var result = SUT.findAllMonthsBelongToYear(-1,"TestOwnerId");

        // then
        assertThat(result).isEqualTo(List.of());
    }

    @Test
    @DisplayName("Should throw NullPointerException when monthRepository returns null instead of List")
    void findAllMonthsBelongToYear_nullInsteadOfListOfMonths_throwsNullPointerException() {
        // given
        var mockMonthRepository = mock(MonthExpensesRepository.class);
        when(mockMonthRepository.findAllByYearIdAndOwnerId(anyInt(), anyString())).thenReturn(null);
        // system under test
        var SUT = new YearExpensesService(null, mockMonthRepository, null);

        // when
        var exception = catchThrowable(() -> SUT.findAllMonthsBelongToYear(-1,"TestOwnerId"));

        // then
        assertThat(exception)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("no months");
    }

    @Test
    @DisplayName("Should throw NotFoundException when no year found")
    void findById_noYearFound_throwsNotFoundException() {
        // given
        var mockRepository = mock(YearExpensesRepository.class);
        when(mockRepository.findByIdAndOwnerId(anyInt(), anyString())).thenReturn(Optional.empty());
        // system under test
        var SUT = new YearExpensesService(mockRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.findById(-1, "ownerTestId"));

        // then
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("with given parameter");
    }

    @Test
    @DisplayName("Should throw NotFoundException when validation has NullPointerException")
    void yearLevelValidationSuccess_throwsNotFoundException() {
        // given
        var mockRepository = mock(YearExpensesRepository.class);
        when(mockRepository.existsByIdAndOwnerId(anyInt(),anyString())).thenThrow(new NullPointerException());
        // system under test
        var SUT = new YearExpensesService(mockRepository, null, null);

        // when
        var exception = catchThrowable(() -> SUT.yearLevelValidationSuccess(-1,"ownerTestId"));

        // then
        assertThat(exception)
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does not exist");
    }
}