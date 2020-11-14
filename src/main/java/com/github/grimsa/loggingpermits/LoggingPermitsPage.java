package com.github.grimsa.loggingpermits;

import com.github.grimsa.infrastructure.jsoup.LoggingConnectionDecorator;
import org.apache.commons.lang3.Validate;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

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

    public List<LoggingPermit> retrieveLoggingPermits(String region) {
        // TODO: temporary
        if (!region.equals("Alytaus TP") && !region.equals("Kauno TP")) {
            return List.of();
        }
        SearchResultsPage firstPage = new SearchResultsPage(new SearchForm(rootPage, region, cookies));
        return Stream.iterate(firstPage, SearchResultsPage::hasNextPage, SearchResultsPage::nextPage)
                // FIXME: temporary
                .limit(2)
                .map(SearchResultsPage::loggingPermits)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private static class SearchForm {
        private final FormElement form;
        private final String region;
        private final Integer pageNumber;
        private final Map<String, String> cookies;

        private SearchForm(Document document, SearchForm searchFormFromPreviousPage, int nextPageNumber) {
            this(document, searchFormFromPreviousPage.region, nextPageNumber, searchFormFromPreviousPage.cookies);
        }

        SearchForm(Document document, String region, Map<String, String> cookies) {
            this(document, region, null, cookies);
        }

        SearchForm(Document document, String region, Integer pageNumber, Map<String, String> cookies) {
            this.form = (FormElement) Objects.requireNonNull(
                    document.getElementById("form1"),
                    () -> "Search form not found in document " + document.text()
            );
            this.region = region;
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
                connection.data("DropDownList1").value(region);
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
            int expectedPageNumber = Optional.ofNullable(pageNumber).orElse(1);
            int currentPage = new PagingInfo(response).currentPage();
            Validate.isTrue(
                    currentPage == expectedPageNumber,
                    "Expected to be on page " + expectedPageNumber + " but we are on " + currentPage
            );
            return response;
        }
    }

    private static class SearchResultsPage {
        private final SearchForm searchForm;
        private final Document document;
        private final PagingInfo pagingInfo;

        SearchResultsPage(SearchForm searchForm) {
            this.searchForm = searchForm;
            this.document = searchForm.submit();
            this.pagingInfo = new PagingInfo(document);
        }

        boolean hasNextPage() {
            return pagingInfo.nextPageNumber().isPresent();
        }

        SearchResultsPage nextPage() {
            Validate.isTrue(hasNextPage(), "Next page must exist");
            return new SearchResultsPage(
                    new SearchForm(document, searchForm, pagingInfo.nextPageNumber().get())
            );
        }

        List<LoggingPermit> loggingPermits() {
            if (pagingInfo.totalPages() == 0) {
                return List.of();
            }
            Element table = document.getElementById("GridView2");
            Objects.requireNonNull(table, () -> "Search results table not found " + document);
            String x = table.selectFirst(":root > tbody > tr").select(":root > th").stream()
                    .map(Element::text)
                    .collect(Collectors.joining(","));
            // TODO: do something smarter about it
            // "Rajonas,Regionas,Agentūra,Nuosavybės forma,Urėdija,Girininkija,Kvartalas,Sklypai,Plotas,Kad. vietovė,Kad. blokas,Kad. nr.,Kirtimo rūšis,Galiojimo pradžia,Galiojimo pabaiga"
            System.out.println("Table header: " + x);
            return table.select(":root > tbody > tr").stream()
                    .map(tr -> tr.select(":root > td"))
                    .filter(not(List::isEmpty))
                    .filter(tds -> tds.size() > 1)
                    .map(HtmlLoggingPermitRow::new)
                    .collect(Collectors.toList());
        }
    }

    private static class PagingInfo {
        private final Element pagingInfoLabel;

        PagingInfo(Document searchResults) {
            this.pagingInfoLabel = Objects.requireNonNull(
                    searchResults.getElementById("Label1"),
                    () -> "Paging info label not found in document " + searchResults.text()
            );
        }

        private int currentPage() {
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

        private Optional<Integer> nextPageNumber() {
            return currentPage() >= totalPages()
                    ? Optional.empty()
                    : Optional.of(currentPage() + 1);
        }
    }

    private static class HtmlLoggingPermitRow implements LoggingPermit {
        private final Elements tds;

        HtmlLoggingPermitRow(Elements tds) {
            this.tds = tds;
        }

        @Override
        public String asTextLine() {
            return tds.stream()
                    .map(Element::text)
                    .collect(Collectors.joining(" | "));
        }
    }
}
