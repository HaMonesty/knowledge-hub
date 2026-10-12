# 用户模块接口文档

## 1. 用户注册

创建普通用户账号。注册成功后，需要调用登录接口获取令牌。

### 基本信息

- 请求方法：POST
- 请求路径：/api/v1/auth/register
- 请求类型：application/json
- 是否需要登录：否

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 4～32 位，英文字母、数字、下划线；服务端统一转为小写 |
| password | string | 是 | 8～64 位，至少包含字母和数字；不自动去除空格 |
| nickname | string | 否 | 1～32 个字符；不传时默认使用用户名 |

请求示例：

```json
{
  "username": "zhangsan",
  "password": "Example123!",
  "nickname": "张三"
}
```

### 成功响应

HTTP 状态码：201 Created

```json
{
  "id": 1,
  "username": "zhangsan",
  "nickname": "张三",
  "role": "USER"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| id | integer(int64) | 用户 ID |
| username | string | 规范化后的用户名 |
| nickname | string | 昵称 |
| role | string | 用户角色，注册时固定为 USER |

### 失败响应

| HTTP 状态码 | 业务错误码 | 说明 |
|---|---|---|
| 400 | VALIDATION_ERROR | 请求参数不符合要求 |
| 409 | USERNAME_ALREADY_EXISTS | 用户名已存在 |

用户名重复示例：

```json
{
  "code": "USERNAME_ALREADY_EXISTS",
  "message": "用户名已存在"
}
```

### 业务规则

1. 用户名不区分大小写，Alice 和 alice 视为同一用户名。
2. 新账号的角色固定为 USER，状态为 ACTIVE。
3. 客户端不能通过请求指定管理员角色。
4. 数据库只保存密码哈希，响应不包含密码或密码哈希。
5. 注册成功不自动登录。
## 2. 用户登录

### 基本信息

- 请求方法：POST
- 请求路径：/api/v1/auth/login
- 请求类型：application/json
- 是否需要登录：否

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 4～32 位，英文字母、数字、下划线；统一转为小写 |
| password | string | 是 | 8～64 位；保持原样，不去除空格 |

请求示例：

```json
{
  "username": "testuser02",
  "password": "Example123!"
}
```

### 成功响应

HTTP 状态码：200

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 1800
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| accessToken | string | 访问令牌 |
| tokenType | string | 固定为 Bearer |
| expiresIn | integer | 有效期，单位为秒 |

### 失败响应

| HTTP 状态码 | 错误码 | 说明 |
|---|---|---|
| 400 | VALIDATION_ERROR | 请求参数不符合要求 |
| 400 | INVALID_REQUEST_BODY | 请求体缺失或 JSON 格式错误 |
| 401 | AUTHENTICATION_FAILED | 账号不存在、密码错误或账号禁用 |
| 500 | INTERNAL_ERROR | 服务端发生异常 |

认证失败示例：

```json
{
  "code": "AUTHENTICATION_FAILED",
  "message": "账号或密码错误"
}
```
## 3. 获取当前登录用户

根据访问令牌确定当前用户身份，返回用户基本信息。

### 基本信息

- 请求方法：GET
- 请求路径：/api/v1/users/me
- 是否需要登录：是
- 请求参数：无
- 请求体：无

### 请求头

| 名称 | 必填 | 说明 |
|---|---|---|
| Authorization | 是 | 格式为 Bearer 空格加访问令牌 |

请求示例：

```http
GET /api/v1/users/me
Authorization: Bearer <登录接口返回的accessToken>
```

### 成功响应

HTTP 状态码：200 OK

```json
{
  "id": 1,
  "username": "testuser02",
  "nickname": "测试用户",
  "role": "USER"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| id | integer(int64) | 用户 ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| role | string | 用户角色，USER 或 ADMIN |

### 失败响应

| HTTP 状态码 | 业务错误码 | 说明 |
|---|---|---|
| 401 | UNAUTHORIZED | 未提供令牌、令牌无效或过期、用户不存在或账号禁用 |
| 500 | INTERNAL_ERROR | 服务端发生异常 |

认证失败示例：

```json
{
  "code": "UNAUTHORIZED",
  "message": "未登录或登录已失效"
}
```

### 业务规则

1. 用户身份从验证通过的 JWT 中取得，不接受客户端指定用户 ID。
2. 后端验证令牌签名、签发者和过期时间。
3. 后端查询数据库，确认用户存在且状态为 ACTIVE。
4. 角色以数据库中的当前值为准。
5. 响应不包含密码或密码哈希。
6. 各种认证失败统一返回相同提示，不暴露内部原因。

### 验收场景

| 场景 | 预期 |
|---|---|
| 携带有效令牌，账号正常 | 200，返回该用户的信息 |
| 不携带 Authorization 请求头 | 401 |
| 携带错误或被篡改的令牌 | 401 |
| 携带已过期令牌 | 401 |
| 令牌对应账号已被禁用 | 401 |