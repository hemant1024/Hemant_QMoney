package com.crio.warmup.stock.portfolio;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  PortfolioManagerImpl(StockQuotesService stockQuotesService){
    this.stockQuotesService = stockQuotesService;
  }

  public PortfolioManagerImpl() {
    this.restTemplate = new RestTemplate();
  }

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.
  
  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF




  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    //     if (from.compareTo(to)>= 0){
    //       throw new RuntimeException();
    //     }
    //     TiingoCandle[] tingu = restTemplate.getForObject(buildUri(symbol, from, to), TiingoCandle[].class);
    //     List<Candle> list= Arrays.asList(tingu);
    //     if (tingu == null) {
    //       return new ArrayList<Candle>();
    // } else {
    //       return list;
    // }
    return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token= "c877a1be3e7ef4c53f0b516f3fa711a20ad561a7";
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
            String url= uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol).
            replace("$STARTDATE", startDate.toString()).replace("$ENDDATE", endDate.toString());
            // System.out.println(url);
            return url;
  }
  public static AnnualizedReturn ana(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totalReturns= (sellPrice - buyPrice)/ buyPrice;
        double total_years= (ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate))/365.2425;
        double annualized_returns= Math.pow((1 + totalReturns), (1/total_years)) -1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) throws JsonProcessingException, StockQuoteServiceException {
        
    AnnualizedReturn annualizedReturn;
    List<AnnualizedReturn> annualizedReturns= new ArrayList<AnnualizedReturn>();

    for(int i=0; i<portfolioTrades.size(); i++){
      annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i), endDate);
      annualizedReturns.add(annualizedReturn);
    }
    Comparator<AnnualizedReturn> SortByAnnReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
    Collections.sort(annualizedReturns, SortByAnnReturn);
    return annualizedReturns;

    // List<AnnualizedReturn> aq= new ArrayList<AnnualizedReturn>();
        // aq= portfolioTrades.stream().map(trade -> {
        //   String link= buildUri(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        //   // System.out.println(link);
        //   TiingoCandle[] tingu = restTemplate.getForObject(link, TiingoCandle[].class);
        //   // System.out.println(tingu);
        //   double openPrice = Arrays.asList(tingu).get(0).getOpen();
        //   double closePrice = Arrays.asList(tingu).get(Arrays.asList(tingu).size() - 1).getClose();

        //   AnnualizedReturn ar= ana(endDate, trade, openPrice, closePrice);
        //   return ar;
        //  }).sorted(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed()).collect(Collectors.toList());
        //  return aq;
  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endLocalDate)throws JsonProcessingException, StockQuoteServiceException{
    AnnualizedReturn annualizedReturn;
    String symbol = trade.getSymbol();
    LocalDate startLocalDate = trade.getPurchaseDate();

    try{
      List<Candle> list;
      list= getStockQuote(symbol, startLocalDate, endLocalDate);
      Candle stockStartDate = list.get(0);
      Candle stockLastest = list.get(list.size()-1);
      Double buyPrice=stockStartDate.getOpen();
      Double sellPrice= stockLastest.getClose();
      Double totalReturn= (sellPrice - buyPrice)/buyPrice;
      Double numYears= (double) ChronoUnit.DAYS.between(startLocalDate, endLocalDate)/365;
      Double annualizedReturns = Math.pow((1 + totalReturn), (1/numYears)) - 1;
      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, totalReturn);
    } catch (JsonProcessingException e){
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    return annualizedReturn;
  }


  @Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
    List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads) 
    throws InterruptedException, StockQuoteServiceException {

  List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
  List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
  final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
  for (int i = 0; i < portfolioTrades.size(); i++) {
    PortfolioTrade trade = portfolioTrades.get(i);
    Callable<AnnualizedReturn> callableTask = () -> {
      return getAnnualizedReturn(trade, endDate);
    };
    Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
    futureReturnsList.add(futureReturns);
  }

  for (int i = 0; i < portfolioTrades.size(); i++) {
    Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
    try {
      AnnualizedReturn returns = futureReturns.get();
      annualizedReturns.add(returns);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException("Error when calling the API", e);

    }
  }
  Collections.sort(annualizedReturns, Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());
  //Collections.sort(annualizedReturns, Collections.reverseOrder());
  return annualizedReturns;
}

  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
