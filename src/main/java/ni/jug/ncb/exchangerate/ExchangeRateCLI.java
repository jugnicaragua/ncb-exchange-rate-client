package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final String HYPHEN = "-";
    private static final String PROMPT = "--> ";

    private static final char COLON = ':';

    private static final String QUERY_BY_DATE = "-date";
    private static final String QUERY_BY_YEAR_MONTH = "-ym";
    private static final String HELP = "--help";

    private static final StringBuilder help = new StringBuilder();
    static {
        help.append("Opciones disponibles:\n");
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
            sb.append(PROMPT + date + COMMA + SPACE + exchangeRate);
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
        if (CLIHelper.containsComma(value)) {
            String[] strDates = CLIHelper.splitCommaSeparatedValue(value);
            for (int i = 0; i < strDates.length; i++) {
                String strDate = strDates[i];
                if (CLIHelper.containsColon(strDate)) {
                    String[] twoDate = CLIHelper.extractTwoValues(strDate, COLON);

                    try {
                        LocalDate date1 = CLIHelper.toLocalDate(twoDate[0]);
                        LocalDate date2 = twoDate[1] == null ? LocalDate.of(date1.getYear(), date1.getMonth(), 1)
                                .plusMonths(1).minusDays(1) : CLIHelper.toLocalDate(twoDate[1]);

                        while (date1.compareTo(date2) <= 0) {
                            doAppendExchangeRateByDate(date1, result);
                            date1 = date1.plusDays(1);
                        }
                    } catch (DateTimeParseException dtpe) {
                        LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [" + strDate + "]");
                    }
                } else {
                    doAppendExchangeRateByDate(strDate, result);
                }
            }
        } else {
            doAppendExchangeRateByDate(value, result);
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
                sb.append(PROMPT + exchangeRateByDate.getKey() + COMMA + SPACE + exchangeRateByDate.getValue() + "\n");
            }
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
        }
    }

    private void doAppendMonthlyExchangeRate(String yearMonth, StringBuilder sb) {
        try {
            LocalDate date = LocalDate.parse(yearMonth + HYPHEN + "01", DateTimeFormatter.ISO_DATE);
            doAppendMonthlyExchangeRate(date, sb);
        } catch (DateTimeParseException dtpe) {
            LOGGER.log(Level.SEVERE, messageForWrongDate(yearMonth));
        }
    }

    private void queryBySpecificYearMonths(String value) {
        LOGGER.info("Obtener tasa de cambio por año-mes");

        StringBuilder result = new StringBuilder("Resultados:");
        if (CLIHelper.containsComma(value)) {
            String[] yearMonths = CLIHelper.splitCommaSeparatedValue(value);
            for (int i = 0; i < yearMonths.length; i++) {
                String yearMonth = yearMonths[i];
                if (CLIHelper.containsColon(yearMonth)) {
                    String[] twoYearMonth = CLIHelper.extractTwoValues(yearMonth, COLON);

                    try {
                        LocalDate date1 = CLIHelper.toLocalDate(twoYearMonth[0] + HYPHEN + "01");
                        LocalDate date2 = twoYearMonth[1] == null ? LocalDate.from(date1) :
                                CLIHelper.toLocalDate(twoYearMonth[1] + HYPHEN + "01");

                        while (date1.compareTo(date2) <= 0) {
                            doAppendMonthlyExchangeRate(date1, result);
                            date1 = date1.plusMonths(1);
                        }
                    } catch (DateTimeParseException dtpe) {
                        LOGGER.log(Level.SEVERE, "No se pudo extraer el rango de fechas del valor [" + yearMonth + "]");
                    }
                } else {
                    doAppendMonthlyExchangeRate(yearMonth, result);
                }
            }
        } else {
            doAppendMonthlyExchangeRate(value, result);
        }

        if (result.length() > 0) {
            LOGGER.info(result.toString());
        }
    }

    public void request(String[] args) {
        // Extraer primero los valores para disparar validaciones
        String queryByDate = CLIHelper.extractOptionValue(QUERY_BY_DATE, args);
        String queryByYearMonth = CLIHelper.extractOptionValue(QUERY_BY_YEAR_MONTH, args);

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

    private static class CLIHelper {

        private static final String HYPHEN_STR = "-";
        private static final String DOUBLE_HYPHEN_STR = "--";
        private static final String EMPTY_STR = "";
        private static final String COMMA = ",";
        private static final String COLON = ":";

        private static final char EQUAL = '=';

        private CLIHelper() {
        }

        private static boolean thereIsNoOptionIndicator(String namedArgument) {
            return !(namedArgument.startsWith(HYPHEN_STR) || namedArgument.startsWith(DOUBLE_HYPHEN_STR));
        }

        private static boolean thereIsNoAssignment(String argument) {
            return argument.indexOf(EQUAL) == -1;
        }

        private static String getValueOfArgument(String argument) {
            int pos = argument.indexOf(EQUAL);
            return pos == argument.length() - 1 ? EMPTY_STR : argument.substring(pos + 1);
        }

        private static void doValidateNamedArgument(String namedArgument) {
            if (thereIsNoOptionIndicator(namedArgument)) {
                throw new IllegalArgumentException("Para extraer el valor de una opción se debe usar guión o guiones en el " +
                        "nombre de la opción [" + namedArgument + "]");
            }
        }

        public static String extractOptionValue(String namedArgument, String[] args) {
            doValidateNamedArgument(namedArgument);

            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith(namedArgument)) {
                    if (thereIsNoAssignment(args[i])) {
                        throw new IllegalArgumentException("Error de sintaxis en parametro " + namedArgument + ", es necesario " +
                                "especificar un valor usando el signo igual (=)");
                    }

                    return getValueOfArgument(args[i]);
                }
            }

            return EMPTY_STR;
        }

        public static boolean optionIsPresent(String namedArgument, String[] args) {
            doValidateNamedArgument(namedArgument);

            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith(namedArgument)) {
                    return true;
                }
            }
            return false;
        }

        public static String[] splitCommaSeparatedValue(String csv) {
            return csv.split("\\,");
        }

        public static boolean containsComma(String value) {
            return value.contains(COMMA);
        }

        public static boolean containsColon(String value) {
            return value.contains(COLON);
        }

        public static String[] extractTwoValues(String value, char delimiter) {
            String[] result = new String[2];
            int pos = value.indexOf(delimiter);
            result[0] = value.substring(0, pos);
            if (pos < value.length() - 1) {
                result[1] = value.substring(pos + 1);
            }
            return result;
        }

        public static LocalDate toLocalDate(String value) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
        }

    }

}
