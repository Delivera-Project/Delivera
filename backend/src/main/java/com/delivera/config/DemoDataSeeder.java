package com.delivera.config;

import com.delivera.model.*;
import com.delivera.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Carga datos de demo cuando el backend arranca con el perfil {@code dev} y la BD no tiene
 * todavía usuarios. Pensado para reseñar la aplicación (gráficas, mapas, listados) sin necesidad
 * de crear los datos manualmente desde la UI.
 *
 * Contraseña común para todos los usuarios: {@code demo1234}
 */
@Component
@Profile("dev")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final SecureRandom RNG = new SecureRandom();

    @Value("${app.demo.seed-password:demo1234}")
    private String seedPassword;

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final CompanyRepository companies;
    private final WorkerRepository workers;
    private final OperationalUnitRepository units;
    private final LoyalUserRepository loyalUsers;
    private final OrderRepository orders;
    private final OrderEventRepository orderEvents;
    private final ActivityTypeRepository activityTypes;
    private final SubscriptionPlanRepository plans;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    public DemoDataSeeder(UserRepository users,
                          OrganizationRepository organizations,
                          CompanyRepository companies,
                          WorkerRepository workers,
                          OperationalUnitRepository units,
                          LoyalUserRepository loyalUsers,
                          OrderRepository orders,
                          OrderEventRepository orderEvents,
                          ActivityTypeRepository activityTypes,
                          SubscriptionPlanRepository plans,
                          PasswordEncoder passwordEncoder) {
        this.users = users;
        this.organizations = organizations;
        this.companies = companies;
        this.workers = workers;
        this.units = units;
        this.loyalUsers = loyalUsers;
        this.orders = orders;
        this.orderEvents = orderEvents;
        this.activityTypes = activityTypes;
        this.plans = plans;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            log.info("DemoDataSeeder: la BD ya tiene usuarios, se omite la carga de demo.");
            return;
        }
        log.info("DemoDataSeeder: cargando datos de demo...");

        // --- 1. Usuarios ---
        User admin    = createUser("admin@delivera.com",    "admin",    "Ana",    "Navarro",   "+34600000001",
                "Paseo de la Castellana 100, Madrid", 40.4430, -3.6900);
        User carlos   = createUser("carlos@rapidlog.com",   "carlos",   "Carlos", "García",    "+34600000002",
                "Calle Goya 60, Madrid",              40.4250, -3.6780);
        User lucia    = createUser("lucia@rapidlog.com",    "lucia",    "Lucía",  "Pérez",     "+34600000003",
                "Calle Alcalá 210, Madrid",           40.4310, -3.6590);
        User marcos   = createUser("marcos@rapidlog.com",   "marcos",   "Marcos", "Ruiz",      "+34600000004",
                "Av. de América 30, Madrid",          40.4380, -3.6720);
        User sofia    = createUser("sofia@transnorte.com",  "sofia",    "Sofía",  "Martínez",  "+34600000005",
                "Gran Vía 8, Bilbao",                 43.2640, -2.9330);
        User david    = createUser("david@transnorte.com",  "david",    "David",  "Sánchez",   "+34600000006",
                "Calle Alfonso I 15, Zaragoza",       41.6530, -0.8790);
        User elena    = createUser("elena@distrisur.com",   "elena",    "Elena",  "Romero",    "+34600000007",
                "Calle Marqués de Larios 5, Málaga",  36.7200, -4.4200);
        User javier   = createUser("javier@distrisur.com",  "javier",   "Javier", "Vargas",    "+34600000008",
                "Av. Andaluces 2, Granada",           37.1780, -3.6090);
        // usuarios "cliente" / fidelizados registrados — con dirección para recibir B2C
        User clara    = createUser("clara@cliente.com",     "clara",    "Clara",  "López",     "+34610000001",
                "Calle Serrano 45, Madrid",     40.4270, -3.6870);
        User raul     = createUser("raul@cliente.com",      "raul",     "Raúl",   "Moreno",    "+34610000002",
                "Av. Andalucía 12, Málaga",     36.7170, -4.4240);
        // usuario "normal" sin roles (para /my-orders)
        createUser("pedro@example.com", "pedro", "Pedro", "Ortiz", "+34610000003",
                "Gran Vía 25, Madrid",         40.4200, -3.7060);

        // --- 2. Organizaciones ---
        Organization rapidlog = createOrg("RapidLog",    "rapidlog");
        Organization transnorte = createOrg("TransNorte", "transnorte");
        Organization distrisur = createOrg("DistriSur",  "distrisur");

        // --- 3. Empresas ---
        ActivityType distribution = activityTypes.findById("DISTRIBUTION").orElseThrow();
        ActivityType food         = activityTypes.findById("FOOD").orElseThrow();
        ActivityType retail       = activityTypes.findById("RETAIL").orElseThrow();
        ActivityType industry     = activityTypes.findById("INDUSTRY").orElseThrow();
        ActivityType transport    = activityTypes.findById("TRANSPORT").orElseThrow();

        SubscriptionPlan free  = plans.findById("FREE").orElseThrow();
        SubscriptionPlan basic = plans.findById("BASIC").orElseThrow();
        SubscriptionPlan pro   = plans.findById("PRO").orElseThrow();

        Company rlCentral = createCompany(rapidlog,   "RapidLog Central",    distribution, pro);
        Company rlRetail  = createCompany(rapidlog,   "RapidLog Retail",     retail,       basic);
        Company tnLog     = createCompany(transnorte, "TransNorte Logística", transport,   basic);
        Company dsFood    = createCompany(distrisur,  "DistriSur Alimentación", food,     pro);
        Company dsInd     = createCompany(distrisur,  "DistriSur Industrial",   industry, free);

        // --- 4. Workers ---
        createWorker(carlos, rlCentral, WorkerRole.COMPANY_ADMIN);
        createWorker(lucia,  rlCentral, WorkerRole.ANALYST);
        createWorker(marcos, rlCentral, WorkerRole.OPERATOR);
        createWorker(carlos, rlRetail,  WorkerRole.COMPANY_ADMIN);
        createWorker(lucia,  rlRetail,  WorkerRole.ANALYST);

        createWorker(sofia,  tnLog,     WorkerRole.COMPANY_ADMIN);
        createWorker(david,  tnLog,     WorkerRole.OPERATOR);

        createWorker(elena,  dsFood,    WorkerRole.COMPANY_ADMIN);
        createWorker(javier, dsFood,    WorkerRole.ANALYST);
        createWorker(elena,  dsInd,     WorkerRole.COMPANY_ADMIN);

        // admin global sobre la primera empresa (para revisar)
        createWorker(admin,  rlCentral, WorkerRole.COMPANY_ADMIN);

        // --- 5. Unidades operativas ---
        // RapidLog Central (distribución, varias ciudades)
        OperationalUnit rlMadridCd = createUnit(rlCentral, "Centro Madrid",    UnitType.LOGISTICS_CENTER,
                "Av. de la Logística 12, Madrid",        40.4168, -3.7038);
        OperationalUnit rlMadridWh = createUnit(rlCentral, "Almacén Getafe",    UnitType.WAREHOUSE,
                "Pol. Ind. Los Olivos, Getafe",          40.3057, -3.7327);
        OperationalUnit rlValencia = createUnit(rlCentral, "Centro Valencia",  UnitType.LOGISTICS_CENTER,
                "Av. del Puerto 200, Valencia",          39.4699, -0.3763);
        OperationalUnit rlSevilla  = createUnit(rlCentral, "Centro Sevilla",   UnitType.LOGISTICS_CENTER,
                "Pol. Ind. Calonge, Sevilla",            37.3891, -5.9845);

        // RapidLog Retail (tiendas)
        OperationalUnit rlTiendaMad = createUnit(rlRetail, "Tienda Madrid Sol", UnitType.STORE,
                "Puerta del Sol 4, Madrid",              40.4167, -3.7037);
        OperationalUnit rlTiendaBcn = createUnit(rlRetail, "Tienda Barcelona",  UnitType.STORE,
                "Passeig de Gràcia 50, Barcelona",       41.3925, 2.1649);

        // TransNorte Logística (norte)
        OperationalUnit tnBilbao = createUnit(tnLog, "Centro Bilbao", UnitType.LOGISTICS_CENTER,
                "Av. del Ferrocarril 22, Bilbao",        43.2630, -2.9350);
        OperationalUnit tnZgz    = createUnit(tnLog, "Almacén Zaragoza", UnitType.WAREHOUSE,
                "Pol. Malpica, Zaragoza",                41.6488, -0.8891);
        OperationalUnit tnVigo   = createUnit(tnLog, "Centro Vigo", UnitType.LOGISTICS_CENTER,
                "Pol. As Gándaras, Vigo",                42.2406, -8.7207);

        // DistriSur Alimentación (sur, alimentación)
        OperationalUnit dsMalaga   = createUnit(dsFood, "Centro Málaga", UnitType.LOGISTICS_CENTER,
                "Av. Velázquez 180, Málaga",             36.7213, -4.4213);
        OperationalUnit dsGranada  = createUnit(dsFood, "Almacén Granada", UnitType.WAREHOUSE,
                "Pol. Juncaril, Granada",                37.1773, -3.5986);
        OperationalUnit dsMurcia   = createUnit(dsFood, "Tienda Murcia", UnitType.STORE,
                "Gran Vía 18, Murcia",                   37.9922, -1.1307);

        // DistriSur Industrial (planta + 1 almacén)
        OperationalUnit dsFactory  = createUnit(dsInd, "Fábrica Jerez", UnitType.FACTORY,
                "Pol. El Portal, Jerez",                 36.6850, -6.1261);
        OperationalUnit dsCadizWh  = createUnit(dsInd, "Almacén Cádiz", UnitType.WAREHOUSE,
                "Zona Franca, Cádiz",                    36.5297, -6.2927);

        // --- 6. Fidelizados ---
        LoyalUser luClara    = createLoyalUser(clara.getEmail(), clara, List.of(rlRetail, dsFood),
                null, null, null);
        LoyalUser luRaul     = createLoyalUser(raul.getEmail(),  raul,  List.of(dsFood),
                null, null, null);
        createLoyalUser("maria.gomez@correo.com",     null, List.of(rlRetail),
                "Calle Mallorca 230, Barcelona", 41.3960, 2.1620);
        LoyalUser luIgnacio  = createLoyalUser("ignacio.serrano@correo.com", null, List.of(rlRetail, dsFood),
                "Av. Diagonal 500, Barcelona",   41.3920, 2.1510);
        createLoyalUser("teresa.mendez@correo.com",   null, List.of(dsFood),
                "Av. Constitución 3, Granada",   37.1770, -3.6000);
        LoyalUser luAlberto  = createLoyalUser("alberto.ibanez@correo.com",  null, List.of(tnLog),
                "Calle Mayor 18, Bilbao",        43.2620, -2.9340);

        // --- 7. Pedidos ---
        // Internos RapidLog Central
        createInternalOrder(rlCentral, rlMadridCd, rlValencia, OrderStatus.DELIVERED, OrderPriority.NORMAL, 11, carlos);
        createInternalOrder(rlCentral, rlMadridCd, rlSevilla,  OrderStatus.IN_TRANSIT, OrderPriority.HIGH,   3, marcos);
        createInternalOrder(rlCentral, rlValencia, rlMadridWh, OrderStatus.PENDING,    OrderPriority.NORMAL, 1, lucia);
        createInternalOrder(rlCentral, rlSevilla,  rlMadridCd, OrderStatus.DELIVERED, OrderPriority.LOW,     9, marcos);
        createInternalOrder(rlCentral, rlMadridWh, rlValencia, OrderStatus.CANCELLED, OrderPriority.NORMAL,  7, carlos);

        // Internos TransNorte
        createInternalOrder(tnLog, tnBilbao, tnZgz,  OrderStatus.DELIVERED,  OrderPriority.NORMAL, 10, sofia);
        createInternalOrder(tnLog, tnZgz,   tnVigo,  OrderStatus.IN_TRANSIT, OrderPriority.HIGH,    2, david);
        createInternalOrder(tnLog, tnVigo,  tnBilbao, OrderStatus.PENDING,   OrderPriority.NORMAL,  0, sofia);

        // Internos DistriSur Alimentación
        createInternalOrder(dsFood, dsMalaga, dsGranada, OrderStatus.DELIVERED, OrderPriority.LOW,    13, elena);
        createInternalOrder(dsFood, dsMalaga, dsMurcia,  OrderStatus.IN_TRANSIT, OrderPriority.HIGH,   4, javier);
        createInternalOrder(dsFood, dsGranada, dsMalaga, OrderStatus.PENDING,    OrderPriority.NORMAL, 0, javier);

        // Internos DistriSur Industrial
        createInternalOrder(dsInd, dsFactory, dsCadizWh, OrderStatus.DELIVERED, OrderPriority.NORMAL, 8, elena);
        createInternalOrder(dsInd, dsCadizWh, dsFactory, OrderStatus.PENDING,   OrderPriority.NORMAL, 0, elena);

        // B2B dentro de la misma org (RapidLog Central → Retail)
        createB2BOrder(rlCentral, rlMadridCd, rlTiendaMad, OrderStatus.DELIVERED,  OrderPriority.NORMAL, 6, carlos);
        createB2BOrder(rlCentral, rlValencia, rlTiendaBcn, OrderStatus.IN_TRANSIT, OrderPriority.HIGH,   2, lucia);
        createB2BOrder(rlCentral, rlMadridWh, rlTiendaMad, OrderStatus.PENDING,    OrderPriority.NORMAL, 0, marcos);

        // B2B cross-org (TransNorte ↔ RapidLog) para demostrar "unidad ajena" lila
        createB2BOrder(tnLog,     tnBilbao,   rlMadridCd,  OrderStatus.IN_TRANSIT, OrderPriority.NORMAL, 1, sofia);
        createB2BOrder(rlCentral, rlValencia, tnVigo,      OrderStatus.PENDING,    OrderPriority.HIGH,   0, carlos);
        createB2BOrder(dsFood,    dsMalaga,   rlTiendaMad, OrderStatus.DELIVERED,  OrderPriority.NORMAL, 5, elena);

        // B2C registrado (loyal user)
        createB2CRegistered(rlRetail, rlTiendaMad, luClara,   OrderStatus.DELIVERED, OrderPriority.NORMAL, 12, carlos);
        createB2CRegistered(rlRetail, rlTiendaBcn, luIgnacio, OrderStatus.IN_TRANSIT, OrderPriority.HIGH,   3, lucia);
        createB2CRegistered(dsFood,   dsMurcia,    luRaul,    OrderStatus.PENDING,    OrderPriority.NORMAL, 0, javier);
        createB2CRegistered(dsFood,   dsGranada,   luClara,   OrderStatus.DELIVERED,  OrderPriority.LOW,    10, elena);
        createB2CRegistered(tnLog,    tnBilbao,    luAlberto, OrderStatus.IN_TRANSIT, OrderPriority.NORMAL, 2, sofia);
        // Pedidos extra para Clara y Raúl (usuarios registrados) — variedad de estados para /my-orders
        createB2CRegistered(rlRetail, rlTiendaMad, luClara,   OrderStatus.IN_TRANSIT, OrderPriority.HIGH,    1, marcos);
        createB2CRegistered(rlRetail, rlTiendaBcn, luClara,   OrderStatus.PENDING,    OrderPriority.NORMAL,  0, lucia);
        createB2CRegistered(dsFood,   dsMalaga,    luRaul,    OrderStatus.DELIVERED,  OrderPriority.NORMAL,  6, javier);
        createB2CRegistered(dsFood,   dsGranada,   luRaul,    OrderStatus.IN_TRANSIT, OrderPriority.HIGH,    1, elena);

        // B2C no registrado (email/nombre/dirección)
        createB2CUnregistered(rlRetail, rlTiendaMad, "nuevo.cliente@mail.com", "Jorge Medina",
                "Calle Alcalá 75, Madrid", 40.4200, -3.6950,
                OrderStatus.PENDING, OrderPriority.NORMAL, 0, carlos);
        createB2CUnregistered(rlRetail, rlTiendaBcn, "laura.sanchez@mail.com", "Laura Sánchez",
                "Av. Meridiana 100, Barcelona", 41.4050, 2.1860,
                OrderStatus.DELIVERED, OrderPriority.HIGH, 7, lucia);
        createB2CUnregistered(dsFood,   dsMurcia,    "antonio.duran@mail.com", "Antonio Durán",
                "Av. Libertad 8, Murcia", 37.9890, -1.1290,
                OrderStatus.IN_TRANSIT, OrderPriority.NORMAL, 2, elena);
        createB2CUnregistered(dsFood,   dsMalaga,    "carmen.salas@mail.com",  "Carmen Salas",
                "Calle Larios 10, Málaga", 36.7200, -4.4210,
                OrderStatus.CANCELLED, OrderPriority.LOW, 5, javier);
        createB2CUnregistered(tnLog,    tnZgz,       "manuel.lopez@mail.com",  "Manuel López",
                "Paseo Independencia 20, Zaragoza", 41.6510, -0.8840,
                OrderStatus.DELIVERED, OrderPriority.NORMAL, 9, sofia);

        log.info("DemoDataSeeder: demo cargada (usuarios={}, empresas={}, unidades={}, pedidos={}).",
                users.count(), companies.count(), units.count(), orders.count());
    }

    // ----- helpers -----

    @SuppressWarnings("java:S107")
    private User createUser(String email, String username, String first, String last, String phone,
                            String address, Double lat, Double lon) {
        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPhone(phone);
        if (address != null) u.setAddress(address);
        if (lat != null) u.setLatitude(BigDecimal.valueOf(lat));
        if (lon != null) u.setLongitude(BigDecimal.valueOf(lon));
        u.setPasswordHash(passwordEncoder.encode(seedPassword));
        return users.save(u);
    }

    private Organization createOrg(String name, String handle) {
        Organization o = new Organization();
        o.setName(name);
        o.setHandle(handle);
        return organizations.save(o);
    }

    private Company createCompany(Organization org, String name, ActivityType activity, SubscriptionPlan plan) {
        Company c = new Company();
        c.setOrganization(org);
        c.setName(name);
        c.setActivityType(activity);
        c.setPlan(plan);
        return companies.save(c);
    }

    private Worker createWorker(User user, Company company, WorkerRole role) {
        Worker w = new Worker();
        w.setUser(user);
        w.setCompany(company);
        w.setRole(role);
        return workers.save(w);
    }

    private OperationalUnit createUnit(Company c, String name, UnitType type, String address,
                                       double lat, double lon) {
        OperationalUnit u = new OperationalUnit();
        u.setCompany(c);
        u.setName(name);
        u.setType(type);
        u.setAddress(address);
        u.setLatitude(BigDecimal.valueOf(lat));
        u.setLongitude(BigDecimal.valueOf(lon));
        return units.save(u);
    }

    private LoyalUser createLoyalUser(String email, User user, List<Company> cs,
                                      String address, Double lat, Double lon) {
        LoyalUser lu = new LoyalUser();
        lu.setEmail(email);
        lu.setUser(user);
        lu.getCompanies().addAll(cs);
        if (address != null) lu.setAddress(address);
        if (lat != null) lu.setLatitude(BigDecimal.valueOf(lat));
        if (lon != null) lu.setLongitude(BigDecimal.valueOf(lon));
        return loyalUsers.save(lu);
    }

    // --- Pedidos ---

    private void createInternalOrder(Company c, OperationalUnit origin, OperationalUnit dest,
                                     OrderStatus status, OrderPriority priority, int daysAgo, User author) {
        Order o = newOrder(c, origin, OrderType.INTERNAL, status, priority, daysAgo);
        o.setDestination(dest);
        o.setNotes("Pedido interno entre unidades de " + c.getName() + ".");
        orders.save(o);
        backdate(o, daysAgo);
        addEvents(o, status, author);
    }

    private void createB2BOrder(Company c, OperationalUnit origin, OperationalUnit dest,
                                OrderStatus status, OrderPriority priority, int daysAgo, User author) {
        Order o = newOrder(c, origin, OrderType.B2B, status, priority, daysAgo);
        o.setDestination(dest);
        o.setNotes("Envío B2B a " + dest.getCompany().getName() + ".");
        orders.save(o);
        backdate(o, daysAgo);
        addEvents(o, status, author);
    }

    private void createB2CRegistered(Company c, OperationalUnit origin, LoyalUser loyal,
                                     OrderStatus status, OrderPriority priority, int daysAgo, User author) {
        Order o = newOrder(c, origin, OrderType.B2C, status, priority, daysAgo);
        o.setLoyalUser(loyal);
        o.setRecipientEmail(loyal.getEmail());
        o.setRecipientName(loyal.getUser() != null
                ? (loyal.getUser().getFirstName() + " " + loyal.getUser().getLastName())
                : loyal.getEmail());
        // Snapshot de dirección: prioridad loyal → user
        String addr = loyal.getAddress();
        BigDecimal lat = loyal.getLatitude();
        BigDecimal lon = loyal.getLongitude();
        if (addr == null && loyal.getUser() != null) {
            addr = loyal.getUser().getAddress();
            lat = loyal.getUser().getLatitude();
            lon = loyal.getUser().getLongitude();
        }
        o.setRecipientAddress(addr);
        o.setRecipientLatitude(lat);
        o.setRecipientLongitude(lon);
        o.setTrackingToken(randomToken());
        o.setNotes("Cliente fidelizado.");
        orders.save(o);
        backdate(o, daysAgo);
        addEvents(o, status, author);
    }

    @SuppressWarnings("java:S107")
    private void createB2CUnregistered(Company c, OperationalUnit origin, String email, String name,
                                       String address, double lat, double lon,
                                       OrderStatus status, OrderPriority priority, int daysAgo, User author) {
        Order o = newOrder(c, origin, OrderType.B2C, status, priority, daysAgo);
        o.setRecipientEmail(email);
        o.setRecipientName(name);
        o.setRecipientAddress(address);
        o.setRecipientLatitude(BigDecimal.valueOf(lat));
        o.setRecipientLongitude(BigDecimal.valueOf(lon));
        o.setTrackingToken(randomToken());
        o.setNotes("Cliente no registrado — envío externo.");
        orders.save(o);
        backdate(o, daysAgo);
        addEvents(o, status, author);
    }

    private Order newOrder(Company c, OperationalUnit origin, OrderType type,
                           OrderStatus status, OrderPriority priority, int daysAgo) {
        Order o = new Order();
        o.setCompany(c);
        o.setOrigin(origin);
        o.setOrderType(type);
        o.setStatus(status);
        o.setPriority(priority);
        o.setReference(nextReference(daysAgo));
        return o;
    }

    private String nextReference(int daysAgo) {
        Instant when = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        // ORD-YYYYMMDD-XXXX
        String date = when.toString().substring(0, 10).replace("-", "");
        return "ORD-" + date + "-" + String.format("%04d", RNG.nextInt(9999));
    }

    private void backdate(Order o, int daysAgo) {
        // created_at se asigna en BD (insertable=false), así que lo sobreescribimos con SQL.
        Instant when = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        em.createNativeQuery("UPDATE orders SET created_at = :ts WHERE id = :id")
                .setParameter("ts", java.sql.Timestamp.from(when))
                .setParameter("id", o.getId())
                .executeUpdate();
    }

    private void addEvents(Order o, OrderStatus finalStatus, User author) {
        // Registro lineal PENDING → IN_TRANSIT → DELIVERED/CANCELLED según status final.
        List<OrderStatus> chain = switch (finalStatus) {
            case PENDING    -> List.of(OrderStatus.PENDING);
            case IN_TRANSIT -> List.of(OrderStatus.PENDING, OrderStatus.IN_TRANSIT);
            case DELIVERED  -> List.of(OrderStatus.PENDING, OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED);
            case CANCELLED  -> List.of(OrderStatus.PENDING, OrderStatus.CANCELLED);
        };
        for (OrderStatus s : chain) {
            OrderEvent ev = new OrderEvent();
            ev.setOrder(o);
            ev.setStatus(s);
            ev.setAuthorEmail(author.getEmail());
            ev.setNote(null);
            orderEvents.save(ev);
        }
    }

    private String randomToken() {
        byte[] buf = new byte[24];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
