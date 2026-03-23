package com.delivera.service;

import com.delivera.config.SecurityUtils;
import com.delivera.dto.order.OrderRequest;
import com.delivera.model.OrderType;
import com.delivera.dto.order.OrderStatusRequest;
import com.delivera.exception.InvalidOrderUnitsException;
import com.delivera.exception.OrderNotFoundException;
import com.delivera.model.*;
import com.delivera.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OperationalUnitRepository unitRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private LoyalUserRepository loyalUserRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private AppConfigService appConfigService;
    @InjectMocks private OrderService orderService;

    private UUID companyId;
    private Company company;
    private Organization organization;
    private OperationalUnit origin;
    private OperationalUnit destination;
    private Order order;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("TestOrg");
        organization.setHandle("test-org");

        company = new Company();
        company.setId(companyId);
        company.setName("TestCompany");
        company.setOrganization(organization);

        origin = new OperationalUnit();
        origin.setId(UUID.randomUUID());
        origin.setName("Origin");
        origin.setType(UnitType.WAREHOUSE);
        origin.setCompany(company);

        destination = new OperationalUnit();
        destination.setId(UUID.randomUUID());
        destination.setName("Destination");
        destination.setType(UnitType.STORE);
        destination.setCompany(company);

        order = new Order();
        order.setId(UUID.randomUUID());
        order.setCompany(company);
        order.setReference("DEL-20240101-0001");
        order.setOrigin(origin);
        order.setDestination(destination);
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(OrderPriority.NORMAL);
    }

    @Test
    void getByCompany_returnsMappedList() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findSentOrReceivedByCompanyId(companyId)).thenReturn(List.of(order));

        assertThat(orderService.getByCompany()).hasSize(1);
    }

    @Test
    void getDetail_found() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findByIdForCompany(order.getId(), companyId)).thenReturn(Optional.of(order));

        assertThat(orderService.getDetail(order.getId())).isNotNull();
    }

    @Test
    void getDetail_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findByIdForCompany(id, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getDetail(id))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void create_internalOrder_success() {
        OrderRequest req = new OrderRequest(origin.getId(), destination.getId(), null, null, OrderType.INTERNAL, null, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(securityUtils.getCurrentEmail()).thenReturn("admin@test.com");
        when(unitRepository.findByIdAndCompanyId(origin.getId(), companyId)).thenReturn(Optional.of(origin));
        when(unitRepository.findByIdAndCompanyId(destination.getId(), companyId)).thenReturn(Optional.of(destination));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(orderRepository.nextReferenceSeq()).thenReturn(1L);
        when(orderRepository.save(any())).thenReturn(order);

        assertThat(orderService.create(req)).isNotNull();
    }

    @Test
    void create_sameOriginAndDestination_throws() {
        OrderRequest req = new OrderRequest(origin.getId(), origin.getId(), null, null, OrderType.INTERNAL, null, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(origin.getId(), companyId)).thenReturn(Optional.of(origin));

        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(InvalidOrderUnitsException.class);
    }

    @Test
    void create_originNotFound_throws() {
        UUID badId = UUID.randomUUID();
        OrderRequest req = new OrderRequest(badId, destination.getId(), null, null, OrderType.INTERNAL, null, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(badId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(InvalidOrderUnitsException.class);
    }

    @Test
    void create_destinationNotFound_throws() {
        UUID badId = UUID.randomUUID();
        OrderRequest req = new OrderRequest(origin.getId(), badId, null, null, OrderType.INTERNAL, null, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(unitRepository.findByIdAndCompanyId(origin.getId(), companyId)).thenReturn(Optional.of(origin));
        when(unitRepository.findByIdAndCompanyId(badId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(InvalidOrderUnitsException.class);
    }

    @Test
    void create_externalOrder_success() {
        OrderRequest req = new OrderRequest(origin.getId(), null, "recipient@test.com", "John", OrderType.B2C, null, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(securityUtils.getCurrentEmail()).thenReturn("admin@test.com");
        when(unitRepository.findByIdAndCompanyId(origin.getId(), companyId)).thenReturn(Optional.of(origin));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(orderRepository.nextReferenceSeq()).thenReturn(1L);
        when(loyalUserRepository.findByCompanyIdAndEmail(companyId, "recipient@test.com")).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenReturn(order);

        assertThat(orderService.create(req)).isNotNull();
    }

    @Test
    void updateStatus_success() {
        OrderStatusRequest req = new OrderStatusRequest(OrderStatus.IN_TRANSIT, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(securityUtils.getCurrentEmail()).thenReturn("admin@test.com");
        when(orderRepository.findByIdAndCompanyId(order.getId(), companyId)).thenReturn(Optional.of(order));
        doNothing().when(appConfigService).validateTransition(any(), any());
        when(orderRepository.save(order)).thenReturn(order);

        assertThat(orderService.updateStatus(order.getId(), req)).isNotNull();
    }

    @Test
    void updateStatus_orderNotFound_throws() {
        UUID id = UUID.randomUUID();
        OrderStatusRequest req = new OrderStatusRequest(OrderStatus.IN_TRANSIT, null);
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(id, req))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void delete_success() {
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findByIdAndCompanyId(order.getId(), companyId)).thenReturn(Optional.of(order));

        orderService.delete(order.getId());
        verify(orderRepository).delete(order);
    }

    @Test
    void delete_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(securityUtils.getCurrentCompanyId()).thenReturn(companyId);
        when(orderRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(id))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getPublicByToken_found() {
        order.setTrackingToken("abc123");
        when(orderRepository.findByTrackingToken("abc123")).thenReturn(Optional.of(order));
        assertThat(orderService.getPublicByToken("abc123")).isNotNull();
    }

    @Test
    void getPublicByToken_notFound_throws() {
        when(orderRepository.findByTrackingToken("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getPublicByToken("bad"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getPublicByReference_found() {
        when(orderRepository.findByReference("DEL-20240101-0001")).thenReturn(Optional.of(order));
        assertThat(orderService.getPublicByReference("del-20240101-0001")).isNotNull();
    }

    @Test
    void getPublicByReference_notFound_throws() {
        when(orderRepository.findByReference("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getPublicByReference("unknown"))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
