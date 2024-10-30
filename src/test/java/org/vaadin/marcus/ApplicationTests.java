package org.vaadin.marcus;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;

@QuarkusTest
@WithPlaywright
class ApplicationTests {
	private static final int NUM_COLS = 8;
	private static final int NUM_ROWS = 6;

	private static final String CHAT_ERROR_MESSAGE = "Sorry but an error occurred. Please re-try your request.";
	private static final Message CHAT_INITIAL_MESSAGE = new Message(Role.ASSISTANT, "Welcome to Funnair! How can I help you?");
	private static final Message CHAT_USER_MESSAGE = new Message(Role.YOU, "Hello. My name is Robert Taylor and my booking number is 105. I would like to cancel my booking.");

	private enum Role {
		ASSISTANT, YOU;

		static Role findRole(String roleStr) {
			return Arrays.stream(values())
			             .filter(role -> roleStr.toLowerCase().contains(role.name().toLowerCase()))
			             .findFirst()
			             .orElseThrow(() -> new IllegalArgumentException("Unknown role: %s".formatted(roleStr)));
		}
	}

	private record Message(Role role, String message) {}

	@InjectPlaywright
	BrowserContext browserContext;

	@TestHTTPResource("/")
	URL index;

	@Test
	void chatWorks() {
		var page = getMainPage();
		var sendButton = page.getByRole(AriaRole.BUTTON);

		PlaywrightAssertions.assertThat(sendButton)
		                    .isVisible();

		assertThat(sendButton.textContent())
			.isNotNull()
			.isEqualTo("Send");

		var messageField = page.getByLabel("Message");

		PlaywrightAssertions.assertThat(messageField)
		                    .isVisible();

		assertThat(getMessages(page))
			.singleElement()
			.isEqualTo(CHAT_INITIAL_MESSAGE);

		messageField.fill(CHAT_USER_MESSAGE.message());
		sendButton.click();

		assertThat(getMessages(page))
			.hasSize(2)
			.containsExactly(CHAT_INITIAL_MESSAGE, CHAT_USER_MESSAGE);

		// Wait for the answer text to have at least one piece of text in the answer
		await()
			.atMost(Duration.ofMinutes(5))
			.until(() -> getMessages(page).size() == 3);

		assertThat(getMessages(page))
			.hasSize(3)
			.contains(CHAT_INITIAL_MESSAGE, atIndex(0))
			.contains(CHAT_USER_MESSAGE, atIndex(1))
			.element(2)
			.matches(m -> (m.role() == Role.ASSISTANT) && !m.message().trim().isBlank() && !CHAT_ERROR_MESSAGE.equals(m.message()));
	}

	@Test
	void bookingsTableCorrect() {
		var page = getMainPage();
		var table = page.getByRole(AriaRole.TREEGRID);

		assertThat(table)
			.isNotNull();

		PlaywrightAssertions.assertThat(table)
		                    .isVisible();

		var tableColumns = table.getByRole(AriaRole.COLUMNHEADER).all();
		assertThat(tableColumns)
			.isNotNull()
			.hasSize(NUM_COLS);

		var tableCells = page.locator("//vaadin-grid-cell-content")
		                     .all()
		                     .stream()
		                     .map(Locator::textContent)
		                     .filter(v -> !v.isBlank())
		                     .toList();

		assertThat(tableCells)
			.isNotNull()
			.hasSize(NUM_COLS * NUM_ROWS);

		var rows = IntStream.range(0, tableCells.size())
		                    .boxed()
		                    .collect(groupingBy(index -> index / NUM_COLS, mapping(tableCells::get, toList())));

		assertThat(rows)
			.hasSize(NUM_ROWS);

		// Assert the column headers
		assertThat(rows.get(0))
			.hasSize(NUM_COLS)
			.containsExactly(
				"#",
				"First name",
				"Last name",
				"Date",
				"From",
				"To",
				"Status",
				"Booking class"
			);

		// In the actual grid, the date/From/to/booking class columns are generated dynamically
		// So we can only assert the booking #, First name, & Last name columns
		assertThat(rows.get(1).subList(0, 3))
			.containsExactly("101", "John", "Doe");

		assertThat(rows.get(2).subList(0, 3))
			.containsExactly("102", "Jane", "Smith");

		assertThat(rows.get(3).subList(0, 3))
			.containsExactly("103", "Michael", "Johnson");

		assertThat(rows.get(4).subList(0, 3))
			.containsExactly("104", "Sarah", "Williams");

		assertThat(rows.get(5).subList(0, 3))
			.containsExactly("105", "Robert", "Taylor");
	}

	private Page getMainPage() {
		var page = this.browserContext.newPage();
		var response = page.navigate(this.index.toString());

		assertThat(response)
			.isNotNull()
			.extracting(Response::status)
			.isEqualTo(Status.OK.getStatusCode());

		page.waitForLoadState(LoadState.NETWORKIDLE);

		PlaywrightAssertions.assertThat(page)
		                    .hasTitle("Funnair customer support");

		return page;
	}

	private static Locator getChatResponseLocator(Page page) {
		return page.locator(".mb-l");
	}

	private static List<Message> getMessages(Page page) {
		return getMessages(getChatResponseLocator(page));
	}

	private static List<Message> getMessages(Locator chatResponses) {
		return chatResponses.all()
		                    .stream()
		                    .map(ApplicationTests::getMessage)
		                    .toList();

	}

	private static Message getMessage(Locator chatResponse) {
		var responseChildren = chatResponse.locator("div");
		var role = Role.findRole(responseChildren.nth(0).textContent());
		var message = responseChildren.nth(1).textContent();

		return new Message(role, message);
	}
}