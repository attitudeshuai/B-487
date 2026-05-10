# 前端 Dockerfile - 多阶段构建

# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app

# 配置 npm 淘宝镜像
RUN npm config set registry https://registry.npmmirror.com

# 复制 package.json 并安装依赖
COPY package*.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# 生产阶段 - 使用 Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的 nginx 静态资源
RUN rm -rf ./*

# 复制构建产物
COPY --from=builder /app/dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
