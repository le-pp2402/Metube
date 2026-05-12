# 🔒 Secure Snowflake ID Implementation

## Vấn đề đã giải quyết

**Trước đây:**

```java
User user = new User();
user.setId(123L);  // ❌ Ai cũng có thể thay đổi ID - KHÔNG AN TOÀN!
```

**Bây giờ:**

```java
User user = new User();
user.setId(123L);  // ❌ LỖI BIÊN DỊCH - method không tồn tại!
Long id = user.getId();  // ✅ CHỈ CÓ THỂ ĐỌC, không thể ghi
```

## Cách hoạt động

### 1. Entity User - ID Protected

```java
@Getter
@Setter
@Entity
@EntityListeners(SnowflakeIdListener.class)
public class User {
    @Id
    @Setter(AccessLevel.NONE)  // 🔒 KHÔNG tạo setter public
    Long id;

    String username;
    String password;
    String email;
}
```

**Kết quả:**

- ✅ `user.getId()` - Có thể đọc
- ❌ `user.setId()` - Không tồn tại (compilation error)

### 2. SnowflakeIdListener - Set ID bằng Reflection

```java
@PrePersist
public void generateId(Object entity) {
    Field idField = findIdField(entity.getClass());
    idField.setAccessible(true);  // 🔓 Bypass private/protected

    if (idField.get(entity) == null) {
        Long newId = idGenerator.nextLongId();
        idField.set(entity, newId);  // ✅ Set ID qua Reflection
    }
}
```

**Lợi ích:**

- 🔒 Code nghiệp vụ KHÔNG THỂ thay đổi ID
- ✅ EntityListener vẫn có thể set ID (qua Reflection)
- ✅ JPA vẫn load ID từ database bình thường

### 3. Sử dụng trong Service

```java
@Service
public class UserAuthService {
    // ❌ KHÔNG CẦN inject IdGenerator nữa!

    public User registerUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // ✅ ID sẽ TỰ ĐỘNG được tạo khi save
        userRepository.save(user);  // <- ID được tạo TẠI ĐÂY

        return user;  // ID đã có giá trị
    }
}
```

## Timeline ID được tạo

```
1. new User()
   └─> id = null

2. user.setUsername("john")
   └─> id = null (vẫn chưa có)

3. userRepository.save(user)
   ├─> JPA: @PrePersist event
   ├─> SnowflakeIdListener.generateId()
   ├─> Reflection: idField.set(user, 123456789L)
   └─> id = 123456789 ✅

4. INSERT INTO users (id, username, ...) VALUES (123456789, 'john', ...)
```

## So sánh các cách

| Cách                           | ID có thể thay đổi? | An toàn?      | Khuyến nghị         |
| ------------------------------ | ------------------- | ------------- | ------------------- |
| **Public setId()**             | ✅ Có               | ❌ Không      | ❌ Không dùng       |
| **@Setter(NONE) + Reflection** | ❌ Không            | ✅ Có         | ⭐ **Recommended**  |
| **Protected setId()**          | ⚠️ Trong package    | ⚠️ Trung bình | ✅ OK nếu cần       |
| **Final id**                   | ❌ Không            | ✅✅ Rất tốt  | ❌ JPA không hỗ trợ |

## Ai có thể set ID?

```java
// ✅ EntityListener (qua Reflection)
@PrePersist
public void generateId(Object entity) {
    idField.set(entity, newId);  // OK
}

// ✅ JPA khi load từ database
User user = userRepository.findById(123L);  // OK

// ✅ Constructor với tất cả tham số
User user = new User(123L, "john", "pass", "email");  // OK với @AllArgsConstructor

// ❌ Code nghiệp vụ
user.setId(999L);  // Compilation error!
```

## Test case

```java
@Test
void testIdCannotBeModifiedExternally() {
    User user = new User();

    // ✅ Có thể đọc
    assertNull(user.getId());

    // ❌ Không thể ghi - không compile được
    // user.setId(123L);  // Error: cannot find symbol

    // ✅ Save tự động tạo ID
    userRepository.save(user);
    assertNotNull(user.getId());

    Long originalId = user.getId();

    // ❌ Không thể thay đổi ID sau khi save
    // user.setId(originalId + 1);  // Error: cannot find symbol

    userRepository.save(user);
    assertEquals(originalId, user.getId());  // ID không đổi ✅
}
```

## Lợi ích bảo mật

1. **Ngăn ID injection**: Không thể inject ID tùy ý từ request
2. **Ngăn ID collision**: Mỗi entity đảm bảo có ID duy nhất
3. **Audit trail**: Biết chắc ID chỉ được tạo tại một nơi
4. **Code clarity**: Rõ ràng ai tạo ID, ai không thể tạo
5. **Fail-fast**: Lỗi compile-time, không phải runtime

## Migration từ code cũ

**Trước (không an toàn):**

```java
public User registerUser(String username, String password, String email) {
    Long userId = idGenerator.nextLongId();
    User user = new User();
    user.setId(userId);  // ❌ Phải inject IdGenerator vào Service
    user.setUsername(username);
    // ...
}
```

**Sau (an toàn):**

```java
public User registerUser(String username, String password, String email) {
    User user = new User();
    user.setUsername(username);  // ✅ Không cần lo về ID
    // ID tự động được tạo khi save
    return userRepository.save(user);
}
```

## Khi nào cần ID trước khi save?

Nếu cần ID TRƯỚC KHI save (ví dụ: để tạo URL, generate token, v.v.):

**Cách 1: Inject IdGenerator vào Service (recommended)**

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final IdGenerator idGenerator;
    private final UserRepository userRepository;

    public User createUserWithPreGeneratedId() {
        Long id = idGenerator.nextLongId();  // Tạo ID trước
        String token = generateToken(id);    // Dùng ID này

        // Dùng constructor để set ID
        User user = new User(id, "username", "password", "email");
        return userRepository.save(user);
    }
}
```

**Cách 2: Tạo xong mới lấy ID**

```java
public String createUserAndGetToken() {
    User user = new User();
    user.setUsername("john");
    userRepository.save(user);  // ID được tạo

    return generateToken(user.getId());  // Lấy ID sau khi save
}
```

## Tổng kết

✅ **ID được bảo vệ** - không thể thay đổi từ code nghiệp vụ  
✅ **Tự động** - không cần inject IdGenerator vào mọi nơi  
✅ **An toàn** - compile-time safety  
✅ **Đơn giản** - ít code hơn, ít bug hơn

🔒 **"ID should be set once and never changed"** - Principle đã được enforce bởi compiler!
