package ni.jug.ncb.exchangerate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public final class Dates {

    private static final char HYPHEN = '-';

    private Dates() {
    }

    public static LocalDate getCurrentDateOrLastDayOf(LocalDate pastDate) {
        LocalDate now = LocalDate.now();
        return pastDate.compareTo(now) <= 0 ? now : LocalDate.of(pastDate.getYear(), pastDate.getMonth(), 1).plusMonths(1).minusDays(1);
    }

    public static LocalDate getLastDateOfMonthOf(LocalDate date) {
        return LocalDate.of(date.getYear(), date.getMonth(), 1).plusMonths(1).minusDays(1);
    }

    public static LocalDate toLocalDate(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
    }

    public static LocalDate toFirstDateOfYearMonth(String yearMonth) {
        return LocalDate.parse(yearMonth + HYPHEN + "01", DateTimeFormatter.ISO_DATE);
    }

    public static LocalDate[] getSortedDates(LocalDate date1, LocalDate date2) {
        LocalDate[] dates = new LocalDate[2];
        if (date1.isBefore(date2)) {
            dates[0] = date1;
            dates[1] = date2;
        } else {
            dates[0] = date2;
            dates[1] = date1;
        }
        return dates;
    }

}
