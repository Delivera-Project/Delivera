package com.delivera.dto.order;

import com.delivera.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicOrderResponseTest {

    private Company company;
    private OperationalUnit origin;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setName("Acme");

        origin = new OperationalUnit();
        origin.setName("Almacén Central");
    }

    private Order buildB2cOrder(String recipientEmail, String trackingToken) {
        Order order = new Order();
        order.setCompany(company);
        order.setOrigin(origin);
        order.setRecipientEmail(recipientEmail);
        order.setTrackingToken(trackingToken);
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(OrderPriority.NORMAL);
        return order;
    }

    // --- claimable flag ---

    @Test
    void claimable_noTrackingToken_false() {
        Order order = buildB2cOrder("juan@gmail.com", null);
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.claimable()).isFalse();
    }

    @Test
    void claimable_hasTokenNoLoyalUser_true() {
        Order order = buildB2cOrder("juan@gmail.com", "abc123");
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.claimable()).isTrue();
    }

    @Test
    void claimable_loyalUserWithoutAccount_true() {
        Order order = buildB2cOrder("juan@gmail.com", "abc123");
        LoyalUser lu = new LoyalUser();
        lu.setEmail("juan@gmail.com");
        order.setLoyalUser(lu);

        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.claimable()).isTrue();
    }

    @Test
    void claimable_loyalUserWithAccount_false() {
        Order order = buildB2cOrder("juan@gmail.com", "abc123");
        User registeredUser = new User();
        LoyalUser lu = new LoyalUser();
        lu.setEmail("juan@gmail.com");
        lu.setUser(registeredUser);
        order.setLoyalUser(lu);

        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.claimable()).isFalse();
    }

    // --- recipientEmailHint ---

    @Test
    void hint_claimableWithEmail_masksCorrectly() {
        Order order = buildB2cOrder("juan@gmail.com", "abc123");
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.recipientEmailHint()).isEqualTo("j***@gmail.com");
    }

    @Test
    void hint_singleCharLocal_masksCorrectly() {
        Order order = buildB2cOrder("j@gmail.com", "abc123");
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.recipientEmailHint()).isEqualTo("j***@gmail.com");
    }

    @Test
    void hint_claimableNoRecipientEmail_null() {
        Order order = buildB2cOrder(null, "abc123");
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.recipientEmailHint()).isNull();
    }

    @Test
    void hint_notClaimable_null() {
        Order order = buildB2cOrder("juan@gmail.com", "abc123");
        User registeredUser = new User();
        LoyalUser lu = new LoyalUser();
        lu.setUser(registeredUser);
        order.setLoyalUser(lu);

        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.recipientEmailHint()).isNull();
    }

    @Test
    void hint_noToken_null() {
        Order order = buildB2cOrder("juan@gmail.com", null);
        PublicOrderResponse response = PublicOrderResponse.from(order);
        assertThat(response.recipientEmailHint()).isNull();
    }
}
