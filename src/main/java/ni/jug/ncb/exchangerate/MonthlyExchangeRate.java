package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import ni.jug.ncb.exchangerate.ws.RecuperaTCMesResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public class MonthlyExchangeRate {

    private final Map<LocalDate, BigDecimal> valuesByLocalDate;
    private final LocalDate firstDate;
    private final LocalDate lastDate;
    private final boolean thereIsAGap;
    private final int size;

    public MonthlyExchangeRate(RecuperaTCMesResponse.RecuperaTCMesResult result) {
        valuesByLocalDate = processResult(result);
        if (valuesByLocalDate.isEmpty()) {
            firstDate = null;
            lastDate = null;
            thereIsAGap = false;
            size = 0;
        } else {
            LocalDate _date = valuesByLocalDate.keySet().iterator().next();
            firstDate = LocalDate.of(_date.getYear(), _date.getMonth(), 1);
            lastDate = firstDate.plusMonths(1).minusDays(1);
            thereIsAGap = valuesByLocalDate.size() != ChronoUnit.DAYS.between(firstDate, lastDate.plusDays(1));
            size = valuesByLocalDate.size();
        }
    }

    private Map<LocalDate, BigDecimal> processResult(RecuperaTCMesResponse.RecuperaTCMesResult result) {
        Map<LocalDate, BigDecimal> valuesByDate = new TreeMap<>();
        List<Object> content = result.getContent();

        if (content.isEmpty()) {
            return valuesByDate;
        }

        Element root = (Element) result.getContent().get(0);
        Node exchangeRateNode = root.getFirstChild();
        while (exchangeRateNode != null) {
            String dateStr = null;
            String value = null;

            Node child = exchangeRateNode.getFirstChild();
            while (child != null) {
                if ("Fecha".equals(child.getNodeName())) {
                    dateStr = ((Text) child.getFirstChild()).getData();
                } else if ("Valor".equals(child.getNodeName())) {
                    value = ((Text) child.getFirstChild()).getData();
                }

                if (dateStr != null && value != null) {
                    break;
                }

                child = child.getNextSibling();
            }

            if (dateStr != null && value != null) {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
                valuesByDate.put(date, new BigDecimal(value));
            }

            exchangeRateNode = exchangeRateNode.getNextSibling();
        }

        return valuesByDate;
    }

    public Map<LocalDate, BigDecimal> getMonthlyExchangeRate() {
        return Collections.unmodifiableMap(valuesByLocalDate);
    }

    public BigDecimal getExchangeRate(LocalDate date) {
        Objects.requireNonNull(date);
        return valuesByLocalDate.getOrDefault(date, BigDecimal.ZERO);
    }

    public BigDecimal getExchangeRate() {
        return valuesByLocalDate.getOrDefault(LocalDate.now(), BigDecimal.ZERO);
    }

    public Map<LocalDate, BigDecimal> getExchangeRateBetween(LocalDate date1, LocalDate date2) {
        Objects.requireNonNull(date1);
        Objects.requireNonNull(date2);

        Dates.validateDate1IsBeforeDate2(date1, date2);

        if (date1.compareTo(firstDate) >= 0 && date1.compareTo(lastDate) <= 0 &&
                date2.compareTo(firstDate) >= 0 && date2.compareTo(lastDate) <= 0) {
            Map<LocalDate, BigDecimal> rangeOfValues = new TreeMap<>();
            while (date1.compareTo(date2) <= 0) {
                rangeOfValues.put(date1, valuesByLocalDate.getOrDefault(date1, BigDecimal.ZERO));
                date1 = date1.plusDays(1);
            }
            return Collections.unmodifiableMap(rangeOfValues);
        } else {
            return Collections.emptyMap();
        }
    }

    public BigDecimal getFirstExchangeRate() {
        return valuesByLocalDate.getOrDefault(firstDate, BigDecimal.ZERO);
    }

    public BigDecimal getLastExchangeRate() {
        return valuesByLocalDate.getOrDefault(lastDate, BigDecimal.ZERO);
    }

    public boolean getThereIsAGap() {
        return thereIsAGap;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "MonthlyExchangeRate{" + valuesByLocalDate + '}';
    }

}
