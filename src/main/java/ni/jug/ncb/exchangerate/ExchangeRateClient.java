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

    private void doValidateYear(int year, Month month) {
        doValidateYear(LocalDate.of(year, month, 1));
    }

    private void doValidateYear(LocalDate date) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        if (now.getYear() >= MINIMUM_YEAR && now.getMonth() == Month.DECEMBER && date.getYear() == (now.getYear() + 1) &&
                date.getMonth() == Month.JANUARY) {
            ++currentYear;
        }
        if (date.getYear() < MINIMUM_YEAR || date.getYear() > currentYear) {
            throw new IllegalArgumentException("El año de consulta [" + date.getYear() + "] debe estar entre [" + MINIMUM_YEAR + ", " + currentYear +
                    "] inclusive");
        }
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
        doValidateYear(year, month);
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
