#!/bin/bash

# Metaloom前端项目构建测试脚本
# 用于验证统一URL前缀配置是否正确

echo "开始构建Metaloom前端项目..."

# 清理之前的构建产物
echo "清理之前的构建产物..."
rm -rf dist/

# 设置生产环境并构建
echo "设置生产环境并构建..."
export NODE_ENV=production

# 执行构建命令
echo "执行Vite构建..."
npm run build

# 检查构建结果
if [ -d "dist" ]; then
    echo "构建成功！"
    echo "检查构建产物结构："
    ls -la dist/
    
    echo ""
    echo "检查静态资源路径配置："
    echo "=== index.html内容 ==="
    cat dist/index.html
    
    echo ""
    echo "=== 静态资源目录结构 ==="
    find dist/ -type f -name "*.js" -o -name "*.css" | head -10
    
else
    echo "构建失败！请检查配置。"
    exit 1
fi

echo "构建测试完成！"