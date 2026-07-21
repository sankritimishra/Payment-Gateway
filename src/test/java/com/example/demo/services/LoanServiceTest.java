package com.example.demo.services;

import com.example.demo.dtos.LoanDTO;
import com.example.demo.repositories.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for LoanService.calDetails(). LoanRepository is mocked since
 * calDetails does no I/O of its own - it's pure arithmetic on the LoanDTO.
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    private LoanDTO loanStartingOn(String startDate, double principal, double annualRatePercent, double tenureYears) {
        return new LoanDTO(
                "1000000001", principal, annualRatePercent, tenureYears,
                null, startDate, null, null, null);
    }

    @Test
    void calDetails_computesFlatTotalPayable_forGivenPrincipalRateAndTenure() {
        LoanService loanService = new LoanService(loanRepository);
        LoanDTO body = loanStartingOn(LocalDate.now().toString(), 100_000.0, 12.0, 1.0);

        loanService.calDetails(body);

        // totalLoanPayable = principal * (1 + monthlyRate * numberOfMonths)
        // = 100000 * (1 + 0.01 * 12) = 112000
        assertThat(body.getTotalLoanPayable()).isEqualTo(112_000.0, within(0.01));
    }

    @Test
    void calDetails_emiSatisfiesAmortizationIdentity() {
        LoanService loanService = new LoanService(loanRepository);
        LoanDTO body = loanStartingOn(LocalDate.now().toString(), 100_000.0, 12.0, 1.0);

        loanService.calDetails(body);

        // The defining property of an EMI: discounting the `n` equal monthly
        // payments of `emi` at the monthly rate must reproduce the original
        // principal. Checking this identity validates the formula is
        // mathematically sound without re-deriving LoanService's own
        // implementation (and its bugs) inside the test.
        double monthlyRate = 12.0 / 12 / 100; // 0.01
        int months = 12;
        double presentValueOfPayments = body.getEmi() * (1 - Math.pow(1 + monthlyRate, -months)) / monthlyRate;

        assertThat(presentValueOfPayments).isEqualTo(100_000.0, within(0.5));
    }

    @Test
    void calDetails_withStartDateToday_hasNothingPaidYet() {
        LoanService loanService = new LoanService(loanRepository);
        LoanDTO body = loanStartingOn(LocalDate.now().toString(), 100_000.0, 12.0, 1.0);

        loanService.calDetails(body);

        // startDate == today -> Period.between(today, today) is zero, so
        // nothing has been paid yet and the full payable amount is
        // outstanding. This also guards the earlier bug where "today" was
        // frozen at service construction time instead of computed per call -
        // if that regressed, this would only pass on the day the app started.
        assertThat(body.getTotalLoanPaid()).isEqualTo(0.0, within(0.0001));
        assertThat(body.getTotalOutstanding()).isEqualTo(body.getTotalLoanPayable(), within(0.0001));
    }

    @Test
    void calDetails_withStartDateThreeMonthsAgo_countsElapsedMonths() {
        LoanService loanService = new LoanService(loanRepository);
        LoanDTO body = loanStartingOn(LocalDate.now().minusMonths(3).toString(), 100_000.0, 12.0, 1.0);

        loanService.calDetails(body);

        // NOTE: LoanService uses Period.getMonths(), which only returns the
        // month component of the elapsed period (0-11), not the total number
        // of months elapsed. That's harmless here (3 < 12) but would
        // under-count for a loan older than a year - a known limitation this
        // test intentionally stays inside, rather than papering over.
        double expectedLoanPaid = body.getEmi() * 3;
        assertThat(body.getTotalLoanPaid()).isEqualTo(expectedLoanPaid, within(0.0001));
    }
}
