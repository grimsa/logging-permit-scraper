package com.github.grimsa.loggingpermits.scraper;

import com.github.grimsa.infrastructure.jsoup.LoggingConnectionDecorator;
import org.apache.commons.lang3.Validate;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoggingPermitsPage {
    private Map<String, String> cookies = Map.of();
    private final Document rootPage;

    public LoggingPermitsPage() throws IOException {
        Connection.Response response = decorateConnection(Jsoup.connect("http://www.amvmt.lt/kirtleidimai/")).execute();
        this.cookies = response.cookies();
        this.rootPage = response.parse();
    }

    private Connection decorateConnection(Connection connection) {
        return new LoggingConnectionDecorator(connection)
                .cookies(cookies);
    }

    public List<String> getRegionOptions() {
        return List.of(
                "Alytaus TP",
                "Kauno TP",
                "Klaipėdos TP",
                "Marijampolės TP",
                "Miškų kontrolės skyrius",
                "Panevėžio TP",
                "Šiaulių TP",
                "Utenos TP",
                "Vilniaus TP"
        );
    }

    public List<LoggingPermit> retrieveLoggingPermits(String region, boolean thisYearOnly) {
        SearchResultsPage firstPage = new SearchResultsPage(new SearchForm(rootPage, region, thisYearOnly, cookies));
        return Stream.iterate(Optional.of(firstPage), Optional::isPresent, (Optional<SearchResultsPage> resultsPage) -> resultsPage.flatMap(SearchResultsPage::nextPage))
                .map(Optional::get)
                .map(SearchResultsPage::loggingPermits)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static class SearchForm {
        private static final String REGION_SELECT_ID = "DropDownList1";
        private final FormElement form;
        private final String region;
        private final boolean thisYearOnly;
        private final Integer pageNumber;
        private final Map<String, String> cookies;

        private SearchForm(Document document, SearchForm searchFormFromPreviousPage, int nextPageNumber) {
            this(document, searchFormFromPreviousPage.region, searchFormFromPreviousPage.thisYearOnly, nextPageNumber, searchFormFromPreviousPage.cookies);
        }

        SearchForm(Document document, String region, boolean thisYearOnly, Map<String, String> cookies) {
            this(document, region, thisYearOnly, null, cookies);
        }

        SearchForm(Document document, String region, boolean thisYearOnly, Integer pageNumber, Map<String, String> cookies) {
            this.form = (FormElement) Objects.requireNonNull(
                    document.getElementById("form1"),
                    () -> "Search form not found in document " + document.text()
            );
            this.region = region;
            this.thisYearOnly = thisYearOnly;
            this.pageNumber = pageNumber;
            this.cookies = cookies;
        }

        Document submit() {
            try {
                if (pageNumber != null) {
                    // Browser does not submit input with type "submit" value when navigating to next page
                    form.elements().removeIf(element -> element.id().equals("Button1"));
                }
                Connection connection = new LoggingConnectionDecorator(form.submit())
                        .cookies(cookies);
                connection.data("metai").value(yearRadioButtonId());
                connection.data(REGION_SELECT_ID).value(region);
                if (pageNumber != null) {
                    connection.data("__EVENTTARGET").value("GridView2");
                    connection.data("__EVENTARGUMENT").value("Page$" + pageNumber);
                }
                return validResponse(connection.execute().parse());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private Document validResponse(Document response) {
            Validate.isTrue(
                    response.getElementById(yearRadioButtonId()).hasAttr("checked"),
                    "Expected year radio button to be selected: " + yearRadioButtonId()
            );
            Validate.isTrue(
                    response.getElementById(REGION_SELECT_ID)
                            .getElementsByAttributeValue("selected", "selected").get(0)
                            .attr("value")
                            .equals(region),
                    "Expected region selection to be: " + region
            );
            int expectedPageNumber = Optional.ofNullable(pageNumber).orElse(1);
            int currentPage = new PagingInfo(response).currentPage();
            Validate.isTrue(
                    currentPage == expectedPageNumber,
                    "Expected to be on page " + expectedPageNumber + " but we are on " + currentPage
            );
            return response;
        }

        private String yearRadioButtonId() {
            return thisYearOnly
                    ? "RadioButton1"
                    : "RadioButton2";
        }
    }

    static class SearchResultsPage {
        private final SearchForm searchForm;
        private final Document document;
        private final PagingInfo pagingInfo;

        SearchResultsPage(SearchForm searchForm) {
            this.searchForm = searchForm;
            this.document = searchForm.submit();
            this.pagingInfo = new PagingInfo(document);
        }

        Optional<SearchResultsPage> nextPage() {
            if (!pagingInfo.nextPageNumber().isPresent()) {
                return Optional.empty();
            }
            return Optional.of(
                    new SearchResultsPage(
                            new SearchForm(document, searchForm, pagingInfo.nextPageNumber().get())
                    )
            );
        }

        List<LoggingPermit> loggingPermits() {
            if (pagingInfo.totalPages() == 0) {
                return List.of();
            }
            return new LoggingPermitSearchResultsTable(document).parse();
        }
    }

    static class PagingInfo {
        private final Element pagingInfoLabel;

        PagingInfo(Document searchResults) {
            this.pagingInfoLabel = Objects.requireNonNull(
                    searchResults.getElementById("Label1"),
                    () -> "Paging info label not found in document " + searchResults.text()
            );
        }

        int currentPage() {
            return parseNumber(pagingInfoLabel.textNodes().get(0).text());
        }

        int totalPages() {
            return parseNumber(pagingInfoLabel.textNodes().get(1).text());
        }

        private int parseNumber(String numericString) {
            return Optional.ofNullable(numericString)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .orElseThrow(() -> new IllegalArgumentException("Could not parse integer from paging info label: " + pagingInfoLabel));
        }

        Optional<Integer> nextPageNumber() {
            return currentPage() >= totalPages()
                    ? Optional.empty()
                    : Optional.of(currentPage() + 1);
        }
    }
}
