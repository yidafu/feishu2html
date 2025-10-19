#!/bin/bash

# 飞书文档转HTML - 运行示例脚本
# 使用方式: ./run-example.sh <app_id> <app_secret> <document_id>

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "================================================"
echo "   飞书文档转HTML工具"
echo "================================================"
echo ""

# 检查参数
if [ "$#" -lt 3 ]; then
    echo -e "${RED}错误: 参数不足${NC}"
    echo ""
    echo "使用方式:"
    echo "  ./run-example.sh <app_id> <app_secret> <document_id> [document_id2] [document_id3] ..."
    echo ""
    echo "示例:"
    echo "  ./run-example.sh cli_a1234567890abcde cli_secret123456 doxcnABCDEFGHIJK"
    echo ""
    echo "参数说明:"
    echo "  app_id       - 飞书应用的App ID"
    echo "  app_secret   - 飞书应用的App Secret"
    echo "  document_id  - 要导出的文档ID（可以指定多个）"
    echo ""
    exit 1
fi

APP_ID=$1
APP_SECRET=$2
shift 2
DOCUMENT_IDS="$@"

echo -e "${GREEN}配置信息:${NC}"
echo "  App ID: $APP_ID"
echo "  文档数量: $#"
echo ""

# 检查Gradle
if ! command -v gradle &> /dev/null && [ ! -f "./gradlew" ]; then
    echo -e "${RED}错误: 未找到Gradle或gradlew${NC}"
    echo "请确保已安装Gradle或存在gradlew脚本"
    exit 1
fi

# 选择使用gradlew或gradle
if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
else
    GRADLE_CMD="gradle"
fi

echo -e "${YELLOW}正在启动导出...${NC}"
echo ""

# 构建参数
ARGS="$APP_ID $APP_SECRET $DOCUMENT_IDS"

# 运行程序
$GRADLE_CMD :run --args="$ARGS"

EXIT_CODE=$?

echo ""
echo "================================================"

if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ 导出完成！${NC}"
    echo ""
    echo "输出位置:"
    echo "  HTML文件: ./output/"
    echo "  图片文件: ./output/images/"
    echo "  附件文件: ./output/files/"
else
    echo -e "${RED}✗ 导出失败${NC}"
    echo ""
    echo "常见问题:"
    echo "  1. 检查App ID和App Secret是否正确"
    echo "  2. 确认应用已添加必要权限并发布"
    echo "  3. 确认文档ID正确且应用有访问权限"
fi

echo "================================================"
exit $EXIT_CODE

