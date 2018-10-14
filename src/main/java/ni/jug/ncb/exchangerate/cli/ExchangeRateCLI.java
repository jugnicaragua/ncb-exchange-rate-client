package ni.jug.ncb.exchangerate.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ni.jug.ncb.exchangerate.Dates;
import ni.jug.ncb.exchangerate.ExchangeRateClient;
import ni.jug.ncb.exchangerate.MonthlyExchangeRate;

/**
 *
 * @author Armando Alaniz
 * @version 1.0
 * @since 1.0
 */
public class ExchangeRateCLI {

    private static final Logger LOGGER = Logger.getLogger(ExchangeRateCLI.class.getName());

    private static final String COMMA = ",";
    private static final String SPACE = " ";
    private static final String PROMPT = "--> ";

    private static final String QUERY_BY_DATE = "-date";
    private static final String QUERY_BY_YEAR_MONTH = "-ym";
    private static final String HELP = "--help";

    private static final StringBuilder help = new StringBuilder();
    static {
        help.append("Opciones disponibles:\n");
        help.append("---------------------\n");
        help.append("  -date: se puede consultar por una fecha, lista de fecha o rango de fechas. ");
        help.append("Por ejemplo: -date=[fecha], -date=[fecha1]:[fecha2], -date=[fecha1],[fecha2],...\n");
        help.append("  -ym: se puede consultar por año-mes. ");
        help.append("Por ejemplo: -ym=[año]-[mes], -ym=[año1]-[mes1]:[año2]-[mes2], -ym=[año1]-[mes1],[año2]-[mes2],...\n");
    }

    private String messageForWrongDate(String strDate) {
        return "El valor [" + strDate + "] no es una fecha. Ingrese una fecha en formato ISO";
    }

    private void doAppendExchangeRateByDate(LocalDate date, StringBuilder sb) {
        ExchangeRateClient wsClient = new ExchangeRateClient();
        BigDecimal exchangeRate;

        try {
            exchangeRate = wsClient.getExchangeRate(date);

            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(PROMPT).append(date).append(COMMA).append(SPACE).append(exchangeRate);
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
        }
    }

    private void doAppendExchangeRateByDate(String strDate, StringBuilder sb) {
        try {
            LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ISO_DATE);
            doAppendExchangeRateByDate(date, sb);
        } catch (DateTimeParseException dtpe) {
            LOGGER.log(Level.SEVERE, messageForWrongDate(strDate));
        }
    }

    private void queryBySpecificDates(String value) {
        LOGGER.info("Obtener tasa de cambio por fecha");

        StringBuilder result = new StringBuilder("Resultados:");
        CLIHelper.OptionListValue optionListValue = new CLIHelper.OptionListValue(value);
        for (int i = 0; i < optionListValue.getSize(); i++) {
            Object obj = optionListValue.getValues()[i];

            if (obj instanceof String) {
                doAppendExchangeRateByDate((String) obj, result);
            } else if (obj instanceof CLIHelper.OptionRangeValue) {
                CLIHelper.OptionRangeValue range = (CLIHelper.OptionRangeValue) obj;
                String[] twoDate = range.getRange();

                try {
                    LocalDate date1 = Dates.toLocalDate(twoDate[0]);
                    LocalDate date2 = twoDate[1] == null ? Dates.getLastDateOfMonthOf(date1) : Dates.toLocalDate(twoDate[1]);

                    while (date1.compareTo(date2) <= 0) {
                        doAppendExchangeRateByDate(date1, result);
                        date1 = date1.plusDays(1);
                    }
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [{0}]", range.getRaw());
                }
            } else {
                throw new IllegalStateException("Tipo de dato no reconocido");
            }
        }

        if (result.length() > 0) {
            LOGGER.info(result.toString());
        }
    }

    private void doAppendMonthlyExchangeRate(LocalDate date, StringBuilder sb) {
        ExchangeRateClient wsClient = new ExchangeRateClient();
        MonthlyExchangeRate monthlyExchangeRate;

        try {
            monthlyExchangeRate = wsClient.getMonthlyExchangeRate(date);

            if (sb.length() > 0) {
                sb.append("\n");
            }
            for (Map.Entry<LocalDate, BigDecimal> exchangeRateByDate : monthlyExchangeRate.getMonthlyExchangeRate().entrySet()) {
                sb.append(PROMPT);
                sb.append(exchangeRateByDate.getKey());
                sb.append(COMMA);
                sb.append(SPACE);
                sb.append(exchangeRateByDate.getValue());
                sb.append("\n");
            }
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
        }
    }

    private void doAppendMonthlyExchangeRate(String yearMonth, StringBuilder sb) {
        try {
            LocalDate date = Dates.toFirstDateOfYearMonth(yearMonth);
            doAppendMonthlyExchangeRate(date, sb);
        } catch (DateTimeParseException dtpe) {
            LOGGER.log(Level.SEVERE, messageForWrongDate(yearMonth));
        }
    }

    private void queryBySpecificYearMonths(String value) {
        LOGGER.info("Obtener tasa de cambio por año-mes");

        StringBuilder result = new StringBuilder("Resultados:");
        CLIHelper.OptionListValue optionListValue = new CLIHelper.OptionListValue(value);
        for (int i = 0; i < optionListValue.getSize(); i++) {
            Object obj = optionListValue.getValues()[i];

            if (obj instanceof String) {
                doAppendMonthlyExchangeRate((String) obj, result);
            } else if (obj instanceof CLIHelper.OptionRangeValue) {
                CLIHelper.OptionRangeValue range = (CLIHelper.OptionRangeValue) obj;
                String[] twoYearMonth = range.getRange();

                try {
                    LocalDate date1 = Dates.toFirstDateOfYearMonth(twoYearMonth[0]);
                    LocalDate date2 = twoYearMonth[1] == null ? Dates.getCurrentDateOrLastDayOf(date1) :
                            Dates.toFirstDateOfYearMonth(twoYearMonth[1]);

                    while (date1.compareTo(date2) <= 0) {
                        doAppendMonthlyExchangeRate(date1, result);
                        date1 = date1.plusMonths(1);
                    }
                } catch (DateTimeParseException dtpe) {
                    LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [{0}]", range.getRaw());
                }
            } else {
                throw new IllegalStateException("Tipo de dato no reconocido");
            }
        }

        if (result.length() > 0) {
            LOGGER.info(result.toString());
        }
    }

    public void request(String[] args) {
        // Extraer primero los valores para disparar validaciones
        String queryByDate = CLIHelper.extractOptionRawValue(QUERY_BY_DATE, args);
        String queryByYearMonth = CLIHelper.extractOptionRawValue(QUERY_BY_YEAR_MONTH, args);

        if (!queryByDate.isEmpty()) {
            queryBySpecificDates(queryByDate);
        }
        if (!queryByYearMonth.isEmpty()) {
            queryBySpecificYearMonths(queryByYearMonth);
        }
        if (CLIHelper.optionIsPresent(HELP, args)) {
            printUsage();
        }
    }

    public static ExchangeRateCLI create(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No se especificaron argumentos");
        }
        return new ExchangeRateCLI();
    }

    public static void printUsage() {
        LOGGER.info(help.toString());
    }

    public static void main(String[] args) {
        try {
            ExchangeRateCLI cli = ExchangeRateCLI.create(args);
            cli.request(args);
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
            printUsage();
        }
    }

}
