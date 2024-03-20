
package com.crio.warmup.stock;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<String>();
    for(PortfolioTrade t : trades){
      symbols.add(t.getSymbol());
    }
     return symbols;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/dantehemant-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "141";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate= new RestTemplate();
    List<PortfolioTrade> file1= readTradesFromJson(args[0]);
    List<TotalReturnsDto> dto= new ArrayList<TotalReturnsDto>();
    LocalDate localdate = LocalDate.parse(args[1], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    for(PortfolioTrade t : file1){
      TiingoCandle[] aa = tc(restTemplate, localdate, t);
      TiingoCandle ab = aa[aa.length-1];
      dto.add(new TotalReturnsDto(t.getSymbol(), ab.getClose()));
    }
    List<String> sortedList=dto.stream().sorted(Comparator.comparingDouble(TotalReturnsDto::getClosingPrice)).map(TotalReturnsDto::getSymbol).collect(Collectors.toList());
    
     return sortedList;
  }

  private static TiingoCandle[] tc(RestTemplate restTemplate, LocalDate localdate,
      PortfolioTrade t) {
    String s1 = prepareUrl(t, localdate, "c877a1be3e7ef4c53f0b516f3fa711a20ad561a7");
    TiingoCandle[] aa= restTemplate.getForObject(s1, TiingoCandle[].class);
    return aa;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    PortfolioTrade[] trades = tradesfromfile(filename);
    List<PortfolioTrade> tl = new ArrayList<PortfolioTrade>();
    for(PortfolioTrade t : trades){
      tl.add(t);
    }
     return tl;
  }

  private static PortfolioTrade[] tradesfromfile(String filename)
      throws URISyntaxException, IOException, StreamReadException, DatabindException {
    File file1= resolveFileFromResources(filename);
    ObjectMapper om= getObjectMapper();
    PortfolioTrade[] trades = om.readValue(file1, PortfolioTrade[].class);
    return trades;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     return "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+ trade.getPurchaseDate()+"&endDate="+endDate+"&token="+token;
  }
  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size() - 1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url1= prepareUrl(trade, endDate, token);
    RestTemplate rt= new RestTemplate();
    TiingoCandle[] cl= rt.getForObject(url1, TiingoCandle[].class);
     return Arrays.asList(cl);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        LocalDate localdate = LocalDate.parse(args[1], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        RestTemplate restTemplate= new RestTemplate();
        PortfolioTrade[] pt= tradesfromfile(args[0]);
        List<AnnualizedReturn> aq= new ArrayList<AnnualizedReturn>();
        aq= Arrays.stream(pt).map(trade -> {
          TiingoCandle[] tingu = tc(restTemplate, localdate, trade);
          double openPrice = getOpeningPriceOnStartDate(Arrays.asList(tingu));
          double closePrice = getClosingPriceOnEndDate(Arrays.asList(tingu));

          AnnualizedReturn ar= calculateAnnualizedReturns(localdate, trade, openPrice, closePrice);
          return ar;
         }).sorted(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed()).collect(Collectors.toList());
         return aq;
        }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totalReturns= (sellPrice - buyPrice)/ buyPrice;
        double total_years= (ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate))/365.2425;
        double annualized_returns= Math.pow((1 + totalReturns), (1/total_years)) -1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }

  public static String getToken() {
    return "c877a1be3e7ef4c53f0b516f3fa711a20ad561a7";
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) throws Exception {
    LocalDate endDate = LocalDate.parse(args[1]);
    RestTemplate restTemplate= new RestTemplate();
    PortfolioManager portfolioManager = new PortfolioManagerImpl(restTemplate); // Create an instance
    PortfolioTrade[] portfolioTrades = tradesfromfile(args[0]);
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

  //   printJsonObject(mainCalculateSingleReturn(args));

  // }

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

