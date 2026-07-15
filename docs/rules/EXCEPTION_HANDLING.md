# Exception Handling Rules

> **MỘT pattern duy nhất**: Service throw → GlobalHandler catch → JSON response.

---

## 1. Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException (abstract)
│   ├── EmployeeNotFoundException
│   ├── ProjectNotFoundException
│   └── AllocationNotFoundException
│
└── BusinessException (abstract)
    ├── AllocationExceededException
    └── ProjectCompletedException
```

### Code

```java
// === BASE: NOT FOUND ===
public abstract class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super("%s not found with id: %d".formatted(resource, id));
    }
}

public class EmployeeNotFoundException extends ResourceNotFoundException {
    public EmployeeNotFoundException(Long id) { super("Employee", id); }
}

public class ProjectNotFoundException extends ResourceNotFoundException {
    public ProjectNotFoundException(Long id) { super("Project", id); }
}

// === BASE: BUSINESS ===
public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

public class AllocationExceededException extends BusinessException {
    public AllocationExceededException(int current, int requested) {
        super("Employee allocation exceeds 100%%. Current: %d%%, Requested: %d%%"
            .formatted(current, requested));
    }
}

public class ProjectCompletedException extends BusinessException {
    public ProjectCompletedException(String projectCode) {
        super("Cannot allocate to completed project: " + projectCode);
    }
}
```

> **Rule**: Exception name phải tự giải thích. Message phải chứa context (ID, value).

---

## 2. GlobalExceptionHandler (DUY NHẤT)

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ValidationDetail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ValidationDetail(e.getField(), e.getDefaultMessage()))
            .toList();

        ErrorResponse body = ErrorResponse.builder()
            .status(400).error("Validation Failed")
            .message("Input validation failed")
            .path(getPath(request))
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .details(details)
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest request) {
        log.error("Unexpected error at {}: {}", getPath(request), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // === helpers ===

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .build());
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) return swr.getRequest().getRequestURI();
        return "unknown";
    }
}
```

> **Tại sao catch `ResourceNotFoundException` (abstract) mà không catch từng cái?** — 1 handler cho tất cả resource not found. Cùng logic, khác message. Nếu cần xử lý riêng, có thể thêm `@ExceptionHandler(EmployeeNotFoundException.class)` sau.

---

## 3. ErrorResponse DTO

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationDetail> details;
}

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDetail {
    private String field;
    private String message;
}
```

### JSON mẫu

```json
// 400 Business
{ "status": 400, "error": "Bad Request", "message": "Employee allocation exceeds 100%", "timestamp": "2026-07-15T10:30:00Z" }

// 404 Not Found
{ "status": 404, "error": "Not Found", "message": "Employee not found with id: 999", "timestamp": "2026-07-15T10:30:00Z" }

// 400 Validation
{ "status": 400, "error": "Validation Failed", "message": "Input validation failed", "timestamp": "2026-07-15T10:30:00Z", "details": [{ "field": "email", "message": "Email should be valid" }] }

// 500 Internal
{ "status": 500, "error": "Internal Server Error", "message": "An unexpected error occurred", "timestamp": "2026-07-15T10:30:00Z" }
```

---

## 4. Exception Flow

```
Controller — không catch gì
      ↓
Service — throw khi gặp lỗi (không catch ở đây)
      ↓
GlobalExceptionHandler — catch, log, build ErrorResponse
      ↓
Client nhận JSON format đồng nhất
```

---

## 5. Logging Rules

| Exception Type | Log Level |
|---------------|-----------|
| `ResourceNotFoundException` | `log.warn` |
| `BusinessException` | `log.warn` |
| `MethodArgumentNotValidException` | Không cần log (Spring tự log) |
| `Exception` (catch-all) | `log.error(stack trace)` |

---

## 6. ⚠️ CẤM

- ❌ Catch exception ở Controller.
- ❌ Catch exception ở Service rồi không throw lại.
- ❌ Return ResponseEntity với message thủ công mỗi nơi mỗi kiểu.
- ❌ Throw `Exception` hoặc `RuntimeException` chung chung.
- ❌ Để stack trace lộ ra response client.
