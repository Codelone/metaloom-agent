# Metaloom前端统一URL前缀配置说明

## 概述
本文档说明如何为Metaloom前端项目配置统一的URL前缀，确保所有资源（HTML、JS、CSS、图片等）和路由都使用统一的基础路径。

## 配置位置

### 1. 环境变量配置
在以下文件中配置基础路径：

**开发环境** (`.env`):
```env
VITE_APP_BASE_PATH=/metaloom/
```

**生产环境** (`.env.production`):
```env
VITE_APP_BASE_PATH=/metaloom/
```

### 2. Vite构建配置
在 `vite.config.ts` 中：
```typescript
export default defineConfig(({ mode }: ConfigEnv): UserConfig => {
    const env = loadEnv(mode, root);
    
    return {
        // 项目部署的基础路径 - 统一前缀配置
        base: env.VITE_APP_BASE_PATH || '/metaloom/',
        // ... 其他配置
    };
});
```

### 3. Vue Router配置
在 `src/router/index.ts` 中：
```typescript
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_BASE_PATH || '/metaloom/'),
  routes
});
```

## 工作原理

### 静态资源处理
- **JS文件**: 打包后的JS文件路径会自动加上前缀，如 `/metaloom/static/js/index-abc123.js`
- **CSS文件**: 打包后的CSS文件路径会自动加上前缀，如 `/metaloom/static/css/index-abc123.css`
- **图片资源**: 所有图片和其他静态资源都会使用统一前缀
- **index.html**: HTML文件中引用的所有资源路径都会自动添加前缀

### 路由处理
- Vue Router的 `createWebHistory()` 使用base路径，确保所有前端路由都在统一前缀下
- 例如: `/metaloom/agent/analysis`、`/metaloom/agent/dashboard` 等

## 部署说明

### 开发环境
开发环境下访问地址为：
```
http://localhost:7777/metaloom/
```

### 生产环境
生产环境部署时，需要确保Web服务器配置正确：

**Nginx配置示例**:
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态资源
    location /metaloom/ {
        alias /path/to/metaloom-frontend/dist/;
        try_files $uri $uri/ /metaloom/index.html;
        
        # 静态资源缓存配置
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # 后端API代理
    location /metaloom/api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Apache配置示例**:
```apache
<VirtualHost *:80>
    ServerName your-domain.com
    DocumentRoot /path/to/metaloom-frontend/dist
    
    # 前端路由重写
    <Directory "/path/to/metaloom-frontend/dist">
        RewriteEngine On
        RewriteBase /metaloom/
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteRule . /metaloom/index.html [L]
    </Directory>
    
    # API代理
    ProxyPass /metaloom/api/ http://localhost:8080/api/
    ProxyPassReverse /metaloom/api/ http://localhost:8080/api/
</VirtualHost>
```

## 构建和测试

### 构建命令
```bash
# 开发环境构建
npm run build

# 生产环境构建
NODE_ENV=production npm run build
```

### 验证配置
构建完成后，检查 `dist/index.html` 文件，确认所有资源路径都包含正确的前缀：

```html
<script type="module" crossorigin src="/metaloom/static/js/index-abc123.js"></script>
<link rel="stylesheet" href="/metaloom/static/css/index-abc123.css">
```

### 测试脚本
可以使用 `build-test.sh` 脚本来验证构建结果：
```bash
chmod +x build-test.sh
./build-test.sh
```

## 自定义前缀

如果需要修改前缀，只需修改环境变量：

1. 修改 `.env` 和 `.env.production` 文件中的 `VITE_APP_BASE_PATH`
2. 重新构建项目
3. 更新Web服务器配置

例如，修改为 `/my-app/`：
```env
VITE_APP_BASE_PATH=/my-app/
```

## 注意事项

1. **前缀格式**: 前缀必须以 `/` 开头和结尾，如 `/metaloom/`
2. **路由匹配**: 确保Web服务器配置与前缀路径匹配
3. **API代理**: 如果使用API代理，需要相应调整代理配置
4. **缓存清理**: 修改前缀后，需要清理浏览器缓存
5. **开发调试**: 开发环境下确保代理配置正确处理前缀路径

## 故障排除

### 常见问题

1. **404错误**: 检查Web服务器配置是否正确处理前缀路径
2. **资源加载失败**: 确认静态资源路径配置正确
3. **路由不工作**: 检查Vue Router的base配置
4. **API调用失败**: 确认API代理配置包含正确的前缀

### 调试方法

1. 检查浏览器开发者工具的Network面板
2. 查看构建产物 `dist/index.html` 的资源路径
3. 验证环境变量是否正确加载
4. 确认Web服务器配置文件语法正确