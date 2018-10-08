package ni.jug.ncb.exchangerate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final String COLON = ":";
    private static final String PROMPT = "--> ";

    private static final String QUERY_BY_DATE = "-date";
    private static final String QUERY_BY_YEAR_MONTH = "-ym";

    private void doAppendExchangeRateByDate(LocalDate date, StringBuilder sb) {
        ExchangeRateClient wsClient = new ExchangeRateClient();
        BigDecimal exchangeRate = wsClient.getExchangeRate(date);
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(PROMPT + date + COMMA + SPACE + exchangeRate);
    }

    private void doAppendExchangeRateByDate(String strDate, StringBuilder sb) {
        LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ISO_DATE);
        doAppendExchangeRateByDate(date, sb);
    }

    private void queryBySpecificDates(String value) {
        LOGGER.info("Obtener tasa de cambio por fecha ");

        StringBuilder result = new StringBuilder("Resultados:");
        if (value.contains(COMMA)) {
            String[] strDates = value.split("\\,");
            for (int i = 0; i < strDates.length; i++) {
                String strDate = strDates[i];
                if (strDate.contains(COLON)) {
                    int pos = strDate.indexOf(COLON);
                    String strDate1 = strDate.substring(0, pos);
                    String strDate2 = null;

                    if (pos < strDate.length() - 1) {
                        strDate2 = strDate.substring(pos + 1);
                    }

                    LocalDate date1 = LocalDate.parse(strDate1, DateTimeFormatter.ISO_DATE);
                    LocalDate date2 = strDate2 == null ? LocalDate.of(date1.getYear(), date1.getMonth(), 1).plusMonths(1).minusDays(1) :
                            LocalDate.parse(strDate2, DateTimeFormatter.ISO_DATE);

                    while (date1.compareTo(date2) <= 0) {
                        doAppendExchangeRateByDate(date1, result);
                        date1 = date1.plusDays(1);
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

    public void request(String[] args) {
        String value = CLIHelper.extractArgumentValue(QUERY_BY_DATE, args);

        if (!value.isEmpty()) {
            queryBySpecificDates(value);
        }

    }

    public static ExchangeRateCLI create(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No se especificaron argumentos");
        }
        return new ExchangeRateCLI();
    }

    public static String usage() {
        StringBuilder help = new StringBuilder();
        help.append("Opciones disponibles:\n");
        help.append("  -date: se puede consultar por una fecha, lista de fecha o rango de fechas. ");
        help.append("Por ejemplo: -date=[fecha], -date=[fecha1]:[fecha2], -date=[fecha1],[fecha2],...\n");
        return help.toString();
    }

    public static void main(String[] args) {
        try {
            ExchangeRateCLI cli = ExchangeRateCLI.create(args);
            cli.request(args);
        } catch (IllegalArgumentException iae) {
            LOGGER.log(Level.SEVERE, iae.getMessage());
            LOGGER.info(usage());
        }
    }

    private static class CLIHelper {

        public static final String HYPHEN_STR = "-";
        public static final String EMPTY_STR = "";
        public static final char EQUAL = '=';

        private CLIHelper() {
        }

        private static boolean thereIsNoHyphen(String namedArgument) {
            return !namedArgument.startsWith(HYPHEN_STR);
        }

        private static boolean thereIsNoAssignment(String argument) {
            return argument.indexOf(EQUAL) == -1;
        }

        private static String getValueOfArgument(String argument) {
            int pos = argument.indexOf(EQUAL);
            return pos == argument.length() - 1 ? EMPTY_STR : argument.substring(pos + 1);
        }

        public static String extractArgumentValue(String namedArgument, String[] args) {
            if (thereIsNoHyphen(namedArgument)) {
                namedArgument = "-" + namedArgument;
            }

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
    }

}
