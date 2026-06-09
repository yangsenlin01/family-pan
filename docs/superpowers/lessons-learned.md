# 开发排坑手册

本文档记录家庭云盘项目 Phase 1 开发过程中遇到的典型问题和解决方案，避免重复踩坑。

---

## 一、MyBatis-Plus

### 1.1 逻辑删除字段必须用 `deleteById()`，不能用 `updateById()`

**现象**：调用 `fileInfo.setIsDeleted(1); fileInfoMapper.updateById(fileInfo)` 后，数据库 `is_deleted` 字段未改变。

**原因**：MyBatis-Plus 的全局逻辑删除配置会**自动剥离** `UPDATE` 语句中的 `is_deleted` 字段，禁止手动修改。必须通过 `deleteById()` 触发软删除：

```yaml
# application.yml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

```java
// 正确
fileInfoMapper.deleteById(id);  // → UPDATE file_info SET is_deleted=1 WHERE id=? AND is_deleted=0

// 错误
fileInfo.setIsDeleted(1);
fileInfoMapper.updateById(fileInfo);  // is_deleted 被剥离，UPDATE 不含该字段
```

### 1.2 数字后缀字段需要 `@TableField` 显式映射

**现象**：查询时报 `Unknown column 'thumbnail200' in 'field list'`。

**原因**：MyBatis-Plus 的驼峰转下划线规则无法处理数字后缀：`thumbnail200` → `thumbnail200`（期望 `thumbnail_200`）。

```java
// 正确
@TableField("thumbnail_200")
private String thumbnail200;

@TableField("thumbnail_800")
private String thumbnail800;
```

### 1.3 跨模块同名 Mapper 导致 Bean 冲突

**现象**：`ConflictingBeanDefinitionException: Annotation-specified bean name 'sysUserMapper' for bean class [com.homecloud.file.mapper.SysUserMapper] conflicts with existing, non-compatible bean definition of same name and class [com.homecloud.auth.mapper.SysUserMapper]`

**原因**：`cloud-auth` 和 `cloud-file` 各有一个 `SysUserMapper`，Spring 扫描时发现同名 Bean。

**解决**：将 `cloud-file` 中的重命名为 `SysUserStorageMapper`。

---

## 二、SaToken

### 2.1 `is-share: false` 时连续 `login()` 会使前一个 Token 失效

**现象**：登录接口返回的两个 Token 相同，且 accessToken 立即失效。

**原因**：`is-share: false` 时，对同一 loginId 再次调用 `StpUtil.login()` 会**踢掉**前一次的会话。

```java
// 错误（is-share: false 时）
StpUtil.login(userId);
String accessToken = StpUtil.getTokenValue();  // 有效

StpUtil.login(userId);
String refreshToken = StpUtil.getTokenValue(); // 新 Token，accessToken 已失效！
```

**解决**：设置 `is-share: true`，单 Token 模式，refresh 时重新 login 延长会话。

### 2.2 JWT 模式不生效

**现象**：配置 `token-style: jwt` 且 `sa-token-jwt` 在 classpath，但生成 Token 仍是 UUID 格式（如 `137ecaba-4cd9-...`）。

**排查**：确认 `sa-token-jwt` 及依赖 `hutool-jwt` 均在 fat jar 中，但 JWT 模式仍未激活。原因未完全定位。

**当前方案**：使用 `token-style: uuid` + `sa-token-redis-jackson`（Redis 持久化）。

### 2.3 Redis 集成包名是 `sa-token-redis-jackson`，不是 `sa-token-dao-redis-jackson`

**现象**：`Could not find artifact cn.dev33:sa-token-dao-redis-jackson:jar:1.45.0`

**正确**：
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-jackson</artifactId>
    <!-- NOT sa-token-dao-redis-jackson -->
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

### 2.4 异常处理器必须设置 HTTP 状态码

**现象**：未登录时接口返回 `{"code":401}` 但 HTTP 状态码为 200，前端 axios 拦截器不触发。

**原因**：`@RestControllerAdvice` 方法默认返回 HTTP 200，`Result.fail(401, ...)` 只改了 body 里的 code。

```java
// 正确
@ExceptionHandler(NotLoginException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)  // 必须加，否则 HTTP 200
public Result<Void> handleNotLoginException(NotLoginException e) {
    return Result.fail(401, "请先登录");
}
```

### 2.5 改密后必须注销 Token

**现象**：修改密码后，旧的 Token 仍然有效。

```java
// 正确
public void changePassword(...) {
    // ... 更新密码
    StpUtil.logout(userId);  // 必须注销所有会话
}
```

---

## 三、Spring Boot / Maven

### 3.1 `spring-boot-maven-plugin` 需要显式 `repackage` goal

**现象**：`java -jar cloud-server.jar` 报 "没有主清单属性"。

**原因**：插件缺少 `<executions>` 配置。

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>repackage</goal></goals>
        </execution>
    </executions>
</plugin>
```

### 3.2 JDBC URL 中 `characterEncoding` 必须用 `UTF-8`

**现象**：`java.io.UnsupportedEncodingException: utf8mb4`

**原因**：`utf8mb4` 是 MySQL 字符集名，不是 Java 编码名。Java 编码是 `UTF-8`。

```yaml
# 正确
url: jdbc:mysql://...?characterEncoding=UTF-8

# 错误
url: jdbc:mysql://...?characterEncoding=utf8mb4
```

### 3.3 Profile 加载顺序

**现象**：`--spring.profiles.active=local,dev` 时 local 的凭据被 dev 覆盖。

**原因**：Spring Boot profile 加载顺序——**后面的覆盖前面的**。

```bash
# 正确：local 在最后，覆盖 dev 中的默认值
--spring.profiles.active=dev,local

# 错误：dev 在最后，覆盖 local 中的真实凭据
--spring.profiles.active=local,dev
```

### 3.4 凭据不放配置文件，用环境变量占位符

```yaml
# application-dev.yml（提交到 Git）
spring:
  datasource:
    username: ${SPRING_DATASOURCE_USERNAME:you account}
    password: ${SPRING_DATASOURCE_PASSWORD:you password}

# application-local.yml（gitignore，不提交）
spring:
  datasource:
    username: root
    password: realpassword
```

---

## 四、Vue 3 / Naive UI

### 4.1 `n-layout` 包含 `n-layout-sider` 必须设置 `has-sider`

**现象**：浏览器控制台警告 `You are putting n-layout-sider in a n-layout but haven't set has-sider on the n-layout`，侧边栏渲染异常。

```vue
<n-layout has-sider>
  <n-layout-sider>...</n-layout-sider>
</n-layout>
```

### 4.2 `n-menu` 的 `@update:value` 导航不可靠

**现象**：点击菜单项无响应。

**方案**：改用 `<div>` + `@click` + `router.push()` 实现侧边栏导航，更可控。

### 4.3 `useMessage()` 必须在 `<n-message-provider>` 后代组件中调用

**现象**：`[naive/use-message]: No outer <n-message-provider /> founded`

**原因**：`useMessage()` 在 `App.vue` 中被调用，但它和 `<n-message-provider>` 在同一层。

**方案**：创建 `<MessageSetup />` 子组件，放在 `<n-message-provider>` 内部，在其 `setup` 中调用 `useMessage()` 并挂到 `window.$message`。

### 4.4 刷新后用户信息丢失

**现象**：刷新页面后侧边栏不显示用户名。

**原因**：Pinia store 中只有 Token 持久化到 `localStorage`，`userInfo` 仅在内存中。

**修复**：在 `setUserInfo()` 和 store 初始化时同步 `localStorage`。

---

## 五、部署 / 网络

### 5.1 GitHub 必须走代理

```bash
# git 临时代理（不改全局配置）
git -c http.proxy=http://127.0.0.1:10808 push

# 环境变量方式
HTTPS_PROXY=http://127.0.0.1:10808 curl ...
```

### 5.2 其他网站先测试连通性

```bash
curl -s -m 5 -o /dev/null -w "HTTP %{http_code}" https://example.com
```

---

## 六、调试技巧

1. **后端 SQL 日志**：设置 `mybatis-plus.configuration.log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl`，关键 DEBUG 日志可看到实际 SQL
2. **SaToken 日志**：临时设置 `sa-token.is-log: true` 查看运行时配置
3. **前端网络**：浏览器 DevTools Network 面板查看实际 HTTP 状态码（body 中的 code 字段不可靠）
4. **Token 验证**：`curl -s -o /dev/null -w "%{http_code}" url -H "Authorization: TOKEN"` 快速验证

---

*文档版本: v1.0 | 日期: 2026-06-09*
