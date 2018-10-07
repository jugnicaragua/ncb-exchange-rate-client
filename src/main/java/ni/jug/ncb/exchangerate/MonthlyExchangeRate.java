package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    public MonthlyExchangeRate(RecuperaTCMesResponse.RecuperaTCMesResult result) {
        valuesByLocalDate = processResult(result);
        if (valuesByLocalDate.isEmpty()) {
            firstDate = null;
            lastDate = null;
        } else {
            LocalDate _date = valuesByLocalDate.keySet().iterator().next();
            firstDate = LocalDate.of(_date.getYear(), _date.getMonth(), 1);
            lastDate = firstDate.plusMonths(1).minusDays(1);
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
        return valuesByLocalDate.getOrDefault(date, BigDecimal.ZERO);
    }

    public BigDecimal getExchangeRate() {
        LocalDate now = LocalDate.now();

        if (now.compareTo(firstDate) >= 0 && now.compareTo(lastDate) <= 0) {
            return valuesByLocalDate.getOrDefault(now, BigDecimal.ZERO);
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getFirstExchangeRate() {
        return valuesByLocalDate.getOrDefault(firstDate, BigDecimal.ZERO);
    }

    public BigDecimal getLastExchangeRate() {
        return valuesByLocalDate.getOrDefault(lastDate, BigDecimal.ZERO);
    }

    public boolean thereIsAGap() {
        LocalDate date = LocalDate.from(firstDate);
        while (date.compareTo(lastDate) <= 0) {
            if (!valuesByLocalDate.containsKey(date)) {
                return true;
            }

            date = date.plusDays(1);
        }

        return false;
    }

    @Override
    public String toString() {
        return "MonthlyExchangeRate{" + valuesByLocalDate + '}';
    }

}
