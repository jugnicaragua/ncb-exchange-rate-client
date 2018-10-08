package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;
import ni.jug.ncb.exchangerate.ws.RecuperaTCMesResponse;
import ni.jug.ncb.exchangerate.ws.TipoCambioBCN;
import ni.jug.ncb.exchangerate.ws.TipoCambioBCNSoap;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public class ExchangeRateClient {

    private static final int MINIMUM_YEAR = 2012;

    private TipoCambioBCNSoap getPort() {
        return new TipoCambioBCN().getTipoCambioBCNSoap();
    }

    public void doValidateYear(int year) {
        int currentYear = LocalDate.now().getYear();

        if (year < MINIMUM_YEAR || year > currentYear) {
            throw new IllegalArgumentException("El año de consulta [" + year + "] debe estar entre [" + MINIMUM_YEAR + ", " + currentYear +
                    "] inclusive");
        }
    }

    public void doValidateYear(LocalDate date) {
        doValidateYear(date.getYear());
    }

    public BigDecimal getExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        doValidateYear(date);
        double taxExchange = getPort().recuperaTCDia(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        return new BigDecimal(String.valueOf(taxExchange));
    }

    public BigDecimal getCurrentExchangeRate() {
        return getExchangeRate(LocalDate.now());
    }

    public MonthlyExchangeRate getMonthlyExchangeRate(int year, Month month) {
        Objects.requireNonNull(month);
        doValidateYear(year);
        RecuperaTCMesResponse.RecuperaTCMesResult result = getPort().recuperaTCMes(year, month.getValue());
        return new MonthlyExchangeRate(result);
    }

    public MonthlyExchangeRate getMonthlyExchangeRate(int year, int month) {
        return getMonthlyExchangeRate(year, Month.of(month));
    }

    public MonthlyExchangeRate getMonthlyExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        return getMonthlyExchangeRate(date.getYear(), date.getMonth());
    }

    public MonthlyExchangeRate getCurrentMonthExchangeRate() {
        return getMonthlyExchangeRate(LocalDate.now());
    }

}
