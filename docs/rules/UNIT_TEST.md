# Testing Rules

> Chuẩn testing cho Spring Boot project — Unit Test, Integration Test, và best practices.

---

## 1. Test Pyramid

```
        ╱╲
       ╱  ╲
      ╱ E2E╲              ← Ít: Smoke test, happy path
     ╱──────╲
    ╱Integration╲          ← Vừa: Repository test, Controller test
   ╱──────────────╲
  ╱   Unit Test    ╲      ← Nhiều: Service test, Validator test
 ╱────────────────────╲
```

| Level | Tỉ lệ | Mục đích | Tốc độ |
|-------|-------|----------|--------|
| **Unit Test** | ~70% | Test business logic, service, validator | Rất nhanh |
| **Integration Test** | ~20% | Test repository, controller | Nhanh |
| **E2E Test** | ~10% | Test full flow | Chậm |

---

## 2. Unit Testing Standards

### 2.1 Framework & Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Includes: JUnit 5, Mockito, AssertJ, Hamcrest, MockMvc.

### 2.2 Naming Convention

**Class name**: `{TargetClass}Test`
```
AllocationService → AllocationServiceTest
EmployeeController → EmployeeControllerTest
MaxAllocationValidator → MaxAllocationValidatorTest
```

**Method name** — 3 styles (chọn 1 và áp dụng nhất quán):

```java
// Style 1: should_ExpectedBehavior_When_Condition
void should_ThrowException_When_AllocationExceeds100Percent()

// Style 2: given_Scenario_When_Action_Then_Result
void given_EmployeeWith60Percent_When_Add50Percent_Then_ThrowException()

// Style 3: descriptive English (không test, không should)
void createAllocation_WithExceededTotal_ThrowsException()
```

> **Khuyến nghị**: Style 1 hoặc 3. Dễ đọc, dễ maintain.

### 2.3 Arrange-Act-Assert Pattern

```java
@Test
void should_ThrowException_When_AllocationExceeds100Percent() {
    // Arrange
    Long employeeId = 1L;
    AllocationRequest request = AllocationRequest.builder()
        .employeeId(employeeId)
        .projectId(1L)
        .allocationPercent(50)
        .roleInProject("Developer")
        .startDate(LocalDate.now())
        .build();

    when(employeeRepository.findById(employeeId))
        .thenReturn(Optional.of(createEmployee()));
    when(projectRepository.findById(1L))
        .thenReturn(Optional.of(createProject()));
    when(allocationRepository.sumAllocationByEmployeeId(employeeId))
        .thenReturn(60);  // current = 60% + request 50% = 110% > 100%

    // Act & Assert
    assertThrows(AllocationExceededException.class,
        () -> allocationService.createAllocation(request));
}
```

### 2.4 Mocking Rules

```java
@ExtendWith(MockitoExtension.class)   // Dùng cho Unit Test
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AllocationServiceImpl allocationService;
    // InjectMocks tự động inject các @Mock vào constructor
}
```

### 2.5 What to Mock

| Layer | Mock? | Ghi chú |
|-------|-------|---------|
| Repository | ✅ **Luôn mock** | Không gọi DB thật trong unit test |
| Service khác | ✅ **Luôn mock** | Dependency bên ngoài |
| Validator | ⚠️ Optional | Có thể mock hoặc dùng thật |
| HTTP Request | ✅ **MockMvc** | Cho controller test |
| External API | ✅ **Luôn mock** | Gọi API thật = integration test |

---

## 3. Integration Testing Standards

> **Lưu ý**: `@Autowired` trong test với `@DataJpaTest`/`@WebMvcTest` là chấp nhận được vì đây là framework injection cho test infrastructure (MockMvc, repositories). **Không** copy pattern này vào production code — dùng `@RequiredArgsConstructor` ở service/controller.

### 3.1 Repository Test

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)  // dùng H2
class AllocationRepositoryTest {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Employee savedEmployee;
    private Project savedProject;

    @BeforeEach
    void setUp() {
        // Seed data trước mỗi test
        savedEmployee = employeeRepository.save(createSampleEmployee());
        savedProject = projectRepository.save(createSampleProject());
    }

    @Test
    void sumAllocationByEmployeeId_ShouldReturnCorrectTotal() {
        // Arrange
        allocationRepository.save(Allocation.builder()
            .employee(savedEmployee)
            .project(savedProject)
            .allocationPercent(60)
            .roleInProject("Dev")
            .startDate(LocalDate.now())
            .build());

        // Act
        Integer total = allocationRepository.sumAllocationByEmployeeId(
            savedEmployee.getEmployeeId());

        // Assert
        assertThat(total).isEqualTo(60);
    }
}
```

### 3.2 Controller Test

```java
@WebMvcTest(AllocationController.class)
class AllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationService allocationService;

    @Test
    void createAllocation_ShouldReturn201() throws Exception {
        // Arrange
        AllocationRequest request = AllocationRequest.builder()
            .employeeId(1L)
            .projectId(1L)
            .allocationPercent(60)
            .roleInProject("Backend Developer")
            .startDate(LocalDate.of(2026, 1, 1))
            .build();

        AllocationResponse response = AllocationResponse.builder()
            .id(1L)
            .employeeId(1L)
            .projectId(1L)
            .allocationPercent(60)
            .roleInProject("Backend Developer")
            .build();

        when(allocationService.createAllocation(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.allocationPercent").value(60));
    }

    @Test
    void createAllocation_WithInvalidPercent_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"employeeId\":1,\"projectId\":1," +
                    "\"allocationPercent\":150,\"roleInProject\":\"Dev\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
```

---

## 4. Test Coverage Goals

| Layer | Coverage Target |
|-------|----------------|
| Service | ≥ 90% |
| Controller | ≥ 80% |
| Repository | ≥ 70% (thường là CRUD test) |
| Validator | 100% |
| **Overall** | ≥ 85% |

### Coverage Check (JaCoCo)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

```bash
mvn clean test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

## 5. Test Scenarios Checklist

### 5.1 Service Tests

| Scenario | Type | Test? |
|----------|------|-------|
| Create thành công | Happy path | ✅ |
| Create với allocation exceed 100% | Business rule | ✅ |
| Create với project COMPLETED | Business rule | ✅ |
| Create với employee không tồn tại | Not found | ✅ |
| Create với project không tồn tại | Not found | ✅ |
| Update thành công | Happy path | ✅ |
| Update giảm allocation | Edge case | ✅ |
| Delete allocation | Happy path | ✅ |
| Delete allocation không tồn tại | Not found | ✅ |
| Get workload employee tồn tại | Happy path | ✅ |
| Get workload employee không tồn tại | Not found | ✅ |

### 5.2 Controller Tests

| Scenario | Type | Test? |
|----------|------|-------|
| POST return 201 | Happy path | ✅ |
| POST với invalid body return 400 | Validation | ✅ |
| POST với business error return 400 | Business rule | ✅ |
| GET return 200 | Happy path | ✅ |
| GET không tìm thấy return 404 | Not found | ✅ |
| DELETE return 204 | Happy path | ✅ |
| PUT return 200 | Happy path | ✅ |

### 5.3 Validator Tests

| Validator | Test Cases |
|-----------|-----------|
| `MaxAllocationValidator` | Tổng = 100% ✅, tổng > 100% ❌, update (exclude self) ✅ |
| `ProjectStatusValidator` | ACTIVE ✅, PLANNING ✅, COMPLETED ❌ |

---

## 6. Test Utility Methods

```java
// TestUtils.java — trong src/test/java
public class TestUtils {

    public static Employee createSampleEmployee() {
        return Employee.builder()
            .employeeCode("EMP001")
            .fullName("Tuan Ho Anh")
            .email("tuanha@company.com")
            .role("Senior Developer")
            .department("FSOFT-Q1")
            .build();
    }

    public static Project createSampleProject() {
        return Project.builder()
            .projectCode("NCG")
            .projectName("NCG Training")
            .customer("Internal")
            .status(ProjectStatus.ACTIVE)
            .build();
    }

    public static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

## 7. Testing Best Practices

### DO
✅ Test **behavior**, không test implementation.
✅ Mỗi test method test **một scenario duy nhất**.
✅ Dùng **meaningful assertions** — AssertJ fluently readable.
✅ Dùng `@BeforeEach` cho setup chung.
✅ Clean data sau mỗi test class với `@AfterAll` hoặc `@DirtiesContext`.
✅ Test cả **happy path** và **error path**.
✅ Dùng `@ParameterizedTest` cho multiple inputs.

```java
// ✅ ĐÚNG — Parameterized test
@ParameterizedTest
@CsvSource({"50,50,100", "30,70,100", "10,0,10"})
void createAllocation_WithValidTotal_ShouldSucceed(
        int currentTotal, int requestPercent, int expectedTotal) {
    // ...
}
```

### DON'T
❌ Test getter/setter — Lombok đã test rồi.
❌ Test framework behavior — focus vào business logic.
❌ Dùng `System.out.println` trong test — dùng assertions.
❌ Test quá nhiều implementation detail — khó maintain khi refactor.
❌ Bỏ qua test edge cases (null, empty, boundary values).

### Boundary Values to Test
- allocationPercent: `0`, `1`, `50`, `100`, `101`
- Employee ID: `null`, `-1L`, `0L`, `1L`, `9999L` (not found)
- Start date: `null`, past, future
- Project status: `PLANNING`, `ACTIVE`, `COMPLETED`
- Total allocation: `0`, `50`, `99`, `100`, `101`, `200`

---

## 8. Test File Organization

```
src/test/java/com/company/project/
├── controller/
│   ├── EmployeeControllerTest.java
│   ├── ProjectControllerTest.java
│   ├── AllocationControllerTest.java
│   └── ReportControllerTest.java
├── service/
│   ├── EmployeeServiceTest.java
│   ├── ProjectServiceTest.java
│   ├── AllocationServiceTest.java
│   └── ReportServiceTest.java
├── repository/
│   ├── EmployeeRepositoryTest.java
│   ├── ProjectRepositoryTest.java
│   └── AllocationRepositoryTest.java
├── validator/
│   ├── MaxAllocationValidatorTest.java
│   └── ProjectStatusValidatorTest.java
├── handler/
│   └── GlobalExceptionHandlerTest.java
└── util/
    └── TestUtils.java
```
